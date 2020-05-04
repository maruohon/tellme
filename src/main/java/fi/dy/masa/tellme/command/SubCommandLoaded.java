package fi.dy.masa.tellme.command;

import java.util.Arrays;
import java.util.List;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec2Argument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.IDimensionRetriever;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.EnumArgument;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.datadump.ChunkDump;
import fi.dy.masa.tellme.datadump.DataDump;
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

public class SubCommandLoaded
{
    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("loaded").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createNodesChunks());
        subCommandRootNode.addChild(createNodesDimensions());
        subCommandRootNode.addChild(createNodesEntities(LoadedTarget.ENTITY));
        subCommandRootNode.addChild(createNodesEntities(LoadedTarget.TILE_ENTITY));

        return subCommandRootNode;
    }

    private static int printHelp(CommandSource source)
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

    private static LiteralCommandNode<CommandSource> createNodesChunks()
    {
        LiteralCommandNode<CommandSource> argChunks = Commands.literal("chunks").build();
        argChunks.addChild(createNodesChunksAll());
        argChunks.addChild(createNodesChunksInArea());
        return argChunks;
    }

    private static LiteralCommandNode<CommandSource> createNodesChunksAll()
    {
        LiteralCommandNode<CommandSource> argAreaTypeAll = Commands.literal("all").build();

        ArgumentCommandNode<CommandSource, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> listLoadedChunksAll(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          DataDump.Format.ASCII,
                          CommandUtils::getDimensionFromSource))
                .build();

        ArgumentCommandNode<CommandSource, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
                .executes(c -> listLoadedChunksAll(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class),
                          CommandUtils::getDimensionFromSource))
                .build();

        ArgumentCommandNode<CommandSource, DimensionType> argDimension = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> listLoadedChunksAll(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class),
                          (s) -> DimensionArgument.getDimensionArgument(c, "dimension")))
                .build();

        argAreaTypeAll.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);
        argOutputFormat.addChild(argDimension);

        return argAreaTypeAll;
    }

    private static LiteralCommandNode<CommandSource> createNodesChunksInArea()
    {
        LiteralCommandNode<CommandSource> argAreaTypeInArea = Commands.literal("in-area").build();
        ArgumentCommandNode<CommandSource, ILocationArgument> argStartCorner = Commands.argument("start_corner", Vec2Argument.vec2()).build();
        ArgumentCommandNode<CommandSource, ILocationArgument> argEndCorner = Commands.argument("end_corner", Vec2Argument.vec2()).build();

        ArgumentCommandNode<CommandSource, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> listLoadedChunksInArea(c.getSource(),
                          Vec2Argument.getVec2f(c, "start_corner"),
                          Vec2Argument.getVec2f(c, "end_corner"),
                          c.getArgument("output_type", OutputType.class),
                          DataDump.Format.ASCII,
                          CommandUtils::getDimensionFromSource))
                .build();

        ArgumentCommandNode<CommandSource, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
                .executes(c -> listLoadedChunksInArea(c.getSource(),
                          Vec2Argument.getVec2f(c, "start_corner"),
                          Vec2Argument.getVec2f(c, "end_corner"),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class),
                          CommandUtils::getDimensionFromSource))
                .build();

        ArgumentCommandNode<CommandSource, DimensionType> argDimension = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> listLoadedChunksInArea(c.getSource(),
                          Vec2Argument.getVec2f(c, "start_corner"),
                          Vec2Argument.getVec2f(c, "end_corner"),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class),
                          (s) -> DimensionArgument.getDimensionArgument(c, "dimension")))
                .build();

        argAreaTypeInArea.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);
        argOutputFormat.addChild(argDimension);

        return argAreaTypeInArea;
    }

    private static LiteralCommandNode<CommandSource> createNodesDimensions()
    {
        LiteralCommandNode<CommandSource> argDimensions = Commands.literal("dimensions").build();

        ArgumentCommandNode<CommandSource, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> listLoadedDimensions(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          DataDump.Format.ASCII))
                .build();

        ArgumentCommandNode<CommandSource, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
                .executes(c -> listLoadedDimensions(c.getSource(),
                          c.getArgument("output_type", OutputType.class),
                          c.getArgument("output_format", DataDump.Format.class)))
                .build();

        argDimensions.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);

        return argDimensions;
    }

    private static LiteralCommandNode<CommandSource> createNodesEntities(LoadedTarget target)
    {
        LiteralCommandNode<CommandSource> argEntityType = Commands.literal(target.getArgument()).build();
        ArgumentCommandNode<CommandSource, Grouping> argGrouping = Commands.argument("grouping",
                EnumArgument.<Grouping>create(Arrays.asList(Grouping.values()), Grouping::fromArgument, (e) -> e.getArgument())).build();
        ArgumentCommandNode<CommandSource, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<CommandSource, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create()).build();

        // /tellme loaded <entities | tile-entities> <all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> all [dimension]
        // /tellme loaded <entities | tile-entities> <all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> in-area <x1> <z1> <x2> <z2> [dimension]
        // /tellme loaded <entities | tile-entities> <all | by-chunk | by-type> <to-chat | to-console | to-file> <ascii | csv> in-chunk <chunkX> <chunkZ> [dimension]

        // all
        LiteralCommandNode<CommandSource> argAreaTypeAll = Commands.literal(AreaType.ALL_LOADED.getArgument())
                .executes(c -> listLoadedEntities(target, AreaType.ALL_LOADED, c, CommandUtils::getDimensionFromSource)).build();

        ArgumentCommandNode<CommandSource, DimensionType> argDimensionAll = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> listLoadedEntities(target, AreaType.ALL_LOADED, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"))).build();

        // in-area
        LiteralCommandNode<CommandSource> argAreaTypeInArea = Commands.literal(AreaType.AREA.getArgument())
                .executes(c -> listLoadedEntities(target, AreaType.AREA, c, CommandUtils::getDimensionFromSource)).build();

        ArgumentCommandNode<CommandSource, ILocationArgument> argStartCorner = Commands.argument("start_corner", Vec2Argument.vec2()).build();
        ArgumentCommandNode<CommandSource, ILocationArgument> argEndCorner = Commands.argument("end_corner", Vec2Argument.vec2())
                .executes(c -> listLoadedEntities(target, AreaType.AREA, c, CommandUtils::getDimensionFromSource)).build();

        ArgumentCommandNode<CommandSource, DimensionType> argDimensionInArea = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> listLoadedEntities(target, AreaType.AREA, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"))).build();

        // in-chunk
        LiteralCommandNode<CommandSource> argAreaTypeInChunk = Commands.literal(AreaType.CHUNK.getArgument())
                .executes(c -> listLoadedEntities(target, AreaType.CHUNK, c, CommandUtils::getDimensionFromSource)).build();

        ArgumentCommandNode<CommandSource, ILocationArgument> argChunkCoords = Commands.argument("chunk", Vec2Argument.vec2())
                .executes(c -> listLoadedEntities(target, AreaType.CHUNK, c, CommandUtils::getDimensionFromSource)).build();

        ArgumentCommandNode<CommandSource, DimensionType> argDimensionInChunk = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> listLoadedEntities(target, AreaType.CHUNK, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"))).build();

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

    private static int listLoadedEntities(LoadedTarget target, AreaType areaType, CommandContext<CommandSource> context, IDimensionRetriever dimensionGetter) throws CommandSyntaxException
    {
        CommandSource source = context.getSource();
        World world = TellMe.dataProvider.getWorld(source.getServer(), dimensionGetter.getDimensionFromSource(source));

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
                    Vec2f vec = CommandUtils.getVec2fFromArg(context, "chunk");
                    ChunkPos pos = CommandUtils.getAsChunkPos(vec);
                    processor.processChunksInArea(world, pos, pos);
                    break;
                }

                case AREA:
                {
                    Vec2f vecStart = CommandUtils.getVec2fFromArg(context, "start_corner");
                    Vec2f vecEnd = CommandUtils.getVec2fFromArg(context, "end_corner");
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

    private static int listLoadedChunksAll(CommandSource source, OutputType outputType,
            DataDump.Format format, IDimensionRetriever dimensionGetter) throws CommandSyntaxException
    {
        List<String> lines = ChunkDump.getFormattedChunkDump(format, dimensionGetter.getDimensionFromSource(source));

        if (lines != null)
        {
            OutputUtils.printOutput(lines, outputType, format, "loaded_chunks", source);
        }

        return 1;
    }

    private static int listLoadedChunksInArea(CommandSource source, Vec2f corner1, Vec2f corner2,
            OutputType outputType, DataDump.Format format, IDimensionRetriever dimensionGetter) throws CommandSyntaxException
    {
        BlockPos minPos = CommandUtils.getMinCorner(corner1, corner2);
        BlockPos maxPos = CommandUtils.getMaxCorner(corner1, corner2);
        List<String> lines = ChunkDump.getFormattedChunkDump(format, dimensionGetter.getDimensionFromSource(source), minPos, maxPos);

        if (lines != null)
        {
            OutputUtils.printOutput(lines, outputType, format, "loaded_chunks", source);
        }

        return 1;
    }

    private static int listLoadedDimensions(CommandSource source, OutputType outputType, DataDump.Format format) throws CommandSyntaxException
    {
        List<String> lines = DimensionDump.getLoadedDimensions(format);

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
