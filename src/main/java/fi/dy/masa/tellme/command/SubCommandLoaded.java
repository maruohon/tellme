package fi.dy.masa.tellme.command;

import java.util.List;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.IWorldRetriever;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.GroupingArgument;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.datadump.ChunkDump;
import fi.dy.masa.tellme.datadump.DimensionDump;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorBase;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorEntityCounterPerChunk;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorEntityCounterPerType;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorTileEntityCounterPerChunk;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorTileEntityCounterPerType;
import fi.dy.masa.tellme.util.chunkprocessor.EntitiesLister;
import fi.dy.masa.tellme.util.chunkprocessor.TileEntitiesLister;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandLoaded
{
    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("loaded").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createNodesChunks());
        subCommandRootNode.addChild(createNodesDimensions());
        subCommandRootNode.addChild(createNodesEntities(LoadedTarget.ENTITY));
        subCommandRootNode.addChild(createNodesEntities(LoadedTarget.TILE_ENTITY));

        return subCommandRootNode;
    }

    private static int printHelp(CommandSourceStack source)
    {
        CommandUtils.sendMessage(source, "Lists the loaded chunks, dimensions, entities or TileEntities");
        CommandUtils.sendMessage(source, "Usage: /tellme loaded chunks all <to-chat | to-console | to-file> [ascii | csv] [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme loaded chunks in-area <x1> <z1> <x2> <z2> <to-chat | to-console | to-file> [ascii | csv] [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme loaded dimensions <to-chat | to-console | to-file> [ascii | csv]");
        CommandUtils.sendMessage(source, "Usage: /tellme loaded <entities | tile-entities> <list-all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> all-loaded [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme loaded <entities | tile-entities> <list-all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> in-area <x1> <z1> <x2> <z2> [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme loaded <entities | tile-entities> <list-all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> in-chunk <chunkX> <chunkZ> [dimension]");

        return 1;
    }

    private static LiteralCommandNode<CommandSourceStack> createNodesChunks()
    {
        LiteralCommandNode<CommandSourceStack> argChunks = Commands.literal("chunks").build();
        argChunks.addChild(createNodesChunksAll());
        argChunks.addChild(createNodesChunksInArea());
        return argChunks;
    }

    private static LiteralCommandNode<CommandSourceStack> createNodesChunksAll()
    {
        LiteralCommandNode<CommandSourceStack> argAreaTypeAll = Commands.literal("all").build();

        ArgumentCommandNode<CommandSourceStack, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> listLoadedChunksAll(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          DataDump.Format.ASCII,
                          CommandUtils::getWorldFromCommandSource))
                .build();

        ArgumentCommandNode<CommandSourceStack, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
                .executes(c -> listLoadedChunksAll(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class),
                          CommandUtils::getWorldFromCommandSource))
                .build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> listLoadedChunksAll(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class),
                          (s) -> DimensionArgument.getDimension(c, "dimension")))
                .build();

        argAreaTypeAll.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);
        argOutputFormat.addChild(argDimension);

        return argAreaTypeAll;
    }

    private static LiteralCommandNode<CommandSourceStack> createNodesChunksInArea()
    {
        LiteralCommandNode<CommandSourceStack> argAreaTypeInArea = Commands.literal("in-area").build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argStartCorner = Commands.argument("start_corner", Vec2Argument.vec2()).build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argEndCorner = Commands.argument("end_corner", Vec2Argument.vec2()).build();

        ArgumentCommandNode<CommandSourceStack, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> listLoadedChunksInArea(c.getSource(),
                          Vec2Argument.getVec2(c, "start_corner"),
                          Vec2Argument.getVec2(c, "end_corner"),
                          c.getArgument("output_type", OutputType.class),
                          DataDump.Format.ASCII,
                          CommandUtils::getWorldFromCommandSource))
                .build();

        ArgumentCommandNode<CommandSourceStack, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
                .executes(c -> listLoadedChunksInArea(c.getSource(),
                          Vec2Argument.getVec2(c, "start_corner"),
                          Vec2Argument.getVec2(c, "end_corner"),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class),
                          CommandUtils::getWorldFromCommandSource))
                .build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> listLoadedChunksInArea(c.getSource(),
                          Vec2Argument.getVec2(c, "start_corner"),
                          Vec2Argument.getVec2(c, "end_corner"),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class),
                          (s) -> DimensionArgument.getDimension(c, "dimension")))
                .build();

        argAreaTypeInArea.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);
        argOutputFormat.addChild(argDimension);

        return argAreaTypeInArea;
    }

    private static LiteralCommandNode<CommandSourceStack> createNodesDimensions()
    {
        LiteralCommandNode<CommandSourceStack> argDimensions = Commands.literal("dimensions").build();

        ArgumentCommandNode<CommandSourceStack, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> listLoadedDimensions(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          DataDump.Format.ASCII))
                .build();

        ArgumentCommandNode<CommandSourceStack, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
                .executes(c -> listLoadedDimensions(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class)))
                .build();

        argDimensions.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);

        return argDimensions;
    }

    private static LiteralCommandNode<CommandSourceStack> createNodesEntities(LoadedTarget target)
    {
        LiteralCommandNode<CommandSourceStack> argEntityType = Commands.literal(target.getArgument()).build();
        ArgumentCommandNode<CommandSourceStack, Grouping> argGrouping = Commands.argument("grouping", GroupingArgument.create()).build();
        ArgumentCommandNode<CommandSourceStack, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<CommandSourceStack, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create()).build();

        // /tellme loaded <entities | tile-entities> <all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> all [dimension]
        // /tellme loaded <entities | tile-entities> <all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> in-area <x1> <z1> <x2> <z2> [dimension]
        // /tellme loaded <entities | tile-entities> <all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> in-chunk <chunkX> <chunkZ> [dimension]

        // all
        LiteralCommandNode<CommandSourceStack> argAreaTypeAll = Commands.literal(AreaType.ALL_LOADED.getArgument())
                .executes(c -> listLoadedEntities(target, AreaType.ALL_LOADED, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimensionAll = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> listLoadedEntities(target, AreaType.ALL_LOADED, c, (s) -> DimensionArgument.getDimension(c, "dimension"))).build();

        // in-area
        LiteralCommandNode<CommandSourceStack> argAreaTypeInArea = Commands.literal(AreaType.AREA.getArgument())
                .executes(c -> listLoadedEntities(target, AreaType.AREA, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argStartCorner = Commands.argument("start_corner", Vec2Argument.vec2()).build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argEndCorner = Commands.argument("end_corner", Vec2Argument.vec2())
                .executes(c -> listLoadedEntities(target, AreaType.AREA, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimensionInArea = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> listLoadedEntities(target, AreaType.AREA, c, (s) -> DimensionArgument.getDimension(c, "dimension"))).build();

        // in-chunk
        LiteralCommandNode<CommandSourceStack> argAreaTypeInChunk = Commands.literal(AreaType.CHUNK.getArgument())
                .executes(c -> listLoadedEntities(target, AreaType.CHUNK, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argChunkCoords = Commands.argument("chunk", Vec2Argument.vec2())
                .executes(c -> listLoadedEntities(target, AreaType.CHUNK, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimensionInChunk = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> listLoadedEntities(target, AreaType.CHUNK, c, (s) -> DimensionArgument.getDimension(c, "dimension"))).build();

        argEntityType.addChild(argGrouping);
        argGrouping.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);

        argOutputFormat.addChild(argAreaTypeAll);
        argAreaTypeAll.addChild(argDimensionAll);

        argOutputFormat.addChild(argAreaTypeInArea);
        argAreaTypeInArea.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimensionInArea);

        argOutputFormat.addChild(argAreaTypeInChunk);
        argAreaTypeInChunk.addChild(argChunkCoords);
        argChunkCoords.addChild(argDimensionInChunk);

        return argEntityType;
    }

    private static int listLoadedEntities(LoadedTarget target, AreaType areaType, CommandContext<CommandSourceStack> context, IWorldRetriever dimensionGetter) throws CommandSyntaxException
    {
        CommandSourceStack source = context.getSource();
        Level world = dimensionGetter.getWorldFromSource(source);

        Grouping grouping = context.getArgument("grouping", Grouping.class);
        OutputType outputType = context.getArgument("output_type", OutputType.class);
        DataDump.Format outputFormat = context.getArgument("output_format", DataDump.Format.class);

        ChunkProcessorBase processor = null;

        if (target == LoadedTarget.TILE_ENTITY)
        {
            switch (grouping)
            {
                case LIST_ALL:
                    processor = new TileEntitiesLister(outputFormat);
                    break;
                case BY_CHUNK:
                    processor = new ChunkProcessorTileEntityCounterPerChunk(outputFormat);
                    break;
                case BY_TYPE:
                    processor = new ChunkProcessorTileEntityCounterPerType(outputFormat);
                    break;
            }
        }
        else if (target == LoadedTarget.ENTITY)
        {
            switch (grouping)
            {
                case LIST_ALL:
                    processor = new EntitiesLister(outputFormat);
                    break;
                case BY_CHUNK:
                    processor = new ChunkProcessorEntityCounterPerChunk(outputFormat);
                    break;
                case BY_TYPE:
                    processor = new ChunkProcessorEntityCounterPerType(outputFormat);
                    break;
            }
        }

        if (processor != null)
        {
            switch (areaType)
            {
                case ALL_LOADED:
                    processor.processChunks(TellMe.dataProvider.getLoadedChunks(world));
                    break;

                case CHUNK:
                {
                    Vec2 vec = CommandUtils.getVec2fFromArg(context, "chunk");
                    ChunkPos pos = CommandUtils.getAsChunkPos(vec);
                    processor.processChunksInArea(world, pos, pos);
                    break;
                }

                case AREA:
                {
                    Vec2 vecStart = CommandUtils.getVec2fFromArg(context, "start_corner");
                    Vec2 vecEnd = CommandUtils.getVec2fFromArg(context, "end_corner");
                    ChunkPos pos1 = CommandUtils.getMinCornerChunkPos(vecStart, vecEnd);
                    ChunkPos pos2 = CommandUtils.getMaxCornerChunkPos(vecStart, vecEnd);
                    processor.processChunksInArea(world, pos1, pos2);
                    break;
                }
            }

            DataDump dump = processor.getDump();
            dump.addHeader(0, String.format("Dimension: '%s'", WorldUtils.getDimensionId(world)));

            List<String> lines = dump.getLines();

            if (lines != null)
            {
                OutputUtils.printOutput(lines, outputType, outputFormat, "loaded_" + target.getArgument(), source);
            }
        }

        return 1;
    }

    private static int listLoadedChunksAll(CommandSourceStack source, OutputType outputType,
            DataDump.Format format, IWorldRetriever dimensionGetter) throws CommandSyntaxException
    {
        List<String> lines = ChunkDump.getFormattedChunkDump(format, source.getServer(), dimensionGetter.getWorldFromSource(source));

        if (lines != null)
        {
            OutputUtils.printOutput(lines, outputType, format, "loaded_chunks", source);
        }

        return 1;
    }

    private static int listLoadedChunksInArea(CommandSourceStack source, Vec2 corner1, Vec2 corner2,
                                              OutputType outputType, DataDump.Format format, IWorldRetriever dimensionGetter) throws CommandSyntaxException
    {
        BlockPos minPos = CommandUtils.getMinCorner(corner1, corner2);
        BlockPos maxPos = CommandUtils.getMaxCorner(corner1, corner2);
        List<String> lines = ChunkDump.getFormattedChunkDump(format, source.getServer(), dimensionGetter.getWorldFromSource(source), minPos, maxPos);

        if (lines != null)
        {
            OutputUtils.printOutput(lines, outputType, format, "loaded_chunks", source);
        }

        return 1;
    }

    private static int listLoadedDimensions(CommandSourceStack source, OutputType outputType, DataDump.Format format) throws CommandSyntaxException
    {
        List<String> lines = DimensionDump.getLoadedDimensions(format, source.getServer());

        if (lines != null)
        {
            OutputUtils.printOutput(lines, outputType, format, "loaded_chunks", source);
        }

        return 1;
    }

    public enum LoadedTarget
    {
        ENTITY      ("entities"),
        TILE_ENTITY ("tile-entities");

        private final String argument;

        LoadedTarget(String argument)
        {
            this.argument = argument;
        }

        public String getArgument()
        {
            return this.argument;
        }

        public static LoadedTarget fromArgument(String argument)
        {
            for (LoadedTarget val : values())
            {
                if (val.argument.equalsIgnoreCase(argument))
                {
                    return val;
                }
            }

            return ENTITY;
        }
    }

    public enum AreaType
    {
        ALL_LOADED  ("all-loaded"),
        AREA        ("in-area"),
        CHUNK       ("in-chunk");

        private final String argument;

        AreaType(String argument)
        {
            this.argument = argument;
        }

        public String getArgument()
        {
            return this.argument;
        }

        public static AreaType fromArgument(String argument)
        {
            for (AreaType val : values())
            {
                if (val.argument.equalsIgnoreCase(argument))
                {
                    return val;
                }
            }

            return ALL_LOADED;
        }
    }

    public enum Grouping
    {
        LIST_ALL("list-all"),
        BY_TYPE ("by-type"),
        BY_CHUNK("by-chunk");

        private final String argument;

        Grouping(String argument)
        {
            this.argument = argument;
        }

        public String getArgument()
        {
            return this.argument;
        }

        public static Grouping fromArgument(String argument)
        {
            for (Grouping val : values())
            {
                if (val.argument.equalsIgnoreCase(argument))
                {
                    return val;
                }
            }

            return LIST_ALL;
        }
    }
}
