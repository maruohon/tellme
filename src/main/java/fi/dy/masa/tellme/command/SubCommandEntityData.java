package fi.dy.masa.tellme.command;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.IWorldRetriever;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.SubCommandLoaded.AreaType;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorBase;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorBlockEntityDataDumper;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorEntityDataDumper;
import fi.dy.masa.tellme.util.chunkprocessor.LocateBase.LocateType;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandEntityData
{
    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("entity-data-dump").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createNodesEntities(LocateType.ENTITY));
        subCommandRootNode.addChild(createNodesEntities(LocateType.TILE_ENTITY));

        return subCommandRootNode;
    }

    private static int printHelp(CommandSource source)
    {
        CommandUtils.sendMessage(source, "Dumps the NBT data of entities or TileEntities");
        CommandUtils.sendMessage(source, "Usage: /tellme entity-data-dump <entities | tile-entities> <to-chat | to-console | to-file> <ascii | csv> all-loaded [dimension] [name name ...]");
        CommandUtils.sendMessage(source, "Usage: /tellme entity-data-dump <entities | tile-entities> <to-chat | to-console | to-file> <ascii | csv> in-box <x1> <y1> <z1> <x2> <y2> <z2> [dimension] [name name ...]");
        CommandUtils.sendMessage(source, "Usage: /tellme entity-data-dump <entities | tile-entities> <to-chat | to-console | to-file> <ascii | csv> in-chunk <chunkX> <chunkZ> [dimension] [name name ...]");

        return 1;
    }

    private static LiteralCommandNode<CommandSource> createNodesEntities(LocateType target)
    {
        LiteralCommandNode<CommandSource> argEntityType = Commands.literal(target.getArgument()).build();
        ArgumentCommandNode<CommandSource, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<CommandSource, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create()).build();

        // all
        LiteralCommandNode<CommandSource> argAreaTypeAll = Commands.literal(AreaType.ALL_LOADED.getArgument())
                .executes(c -> dumpEntityData(target, AreaType.ALL_LOADED, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSource, ResourceLocation> argDimensionAll = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> dumpEntityData(target, AreaType.ALL_LOADED, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"))).build();

        // in-box
        LiteralCommandNode<CommandSource> argAreaTypeInArea = Commands.literal("box")
                .executes(c -> dumpEntityData(target, AreaType.AREA, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSource, ILocationArgument> argStartCorner = Commands.argument("start_corner", Vec3Argument.vec3()).build();
        ArgumentCommandNode<CommandSource, ILocationArgument> argEndCorner = Commands.argument("end_corner", Vec3Argument.vec3())
                .executes(c -> dumpEntityData(target, AreaType.AREA, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSource, ResourceLocation> argDimensionInArea = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> dumpEntityData(target, AreaType.AREA, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"))).build();

        // in-chunk
        LiteralCommandNode<CommandSource> argAreaTypeInChunk = Commands.literal(AreaType.CHUNK.getArgument())
                .executes(c -> dumpEntityData(target, AreaType.CHUNK, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSource, ILocationArgument> argChunkCoords = Commands.argument("chunk", Vec2Argument.vec2())
                .executes(c -> dumpEntityData(target, AreaType.CHUNK, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSource, ResourceLocation> argDimensionInChunk = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> dumpEntityData(target, AreaType.CHUNK, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"))).build();

        ArgumentCommandNode<CommandSource, List<String>> argNamesAll = Commands.argument("filters",
                StringCollectionArgument.create(() -> target.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(c -> dumpEntityData(target, AreaType.ALL_LOADED, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"), c.getArgument("filters", List.class))).build();

        ArgumentCommandNode<CommandSource, List<String>> argNamesBox = Commands.argument("filters",
                StringCollectionArgument.create(() -> target.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(c -> dumpEntityData(target, AreaType.AREA, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"), c.getArgument("filters", List.class))).build();

        ArgumentCommandNode<CommandSource, List<String>> argNamesChunk = Commands.argument("filters",
                StringCollectionArgument.create(() -> target.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(c -> dumpEntityData(target, AreaType.CHUNK, c, (s) -> DimensionArgument.getDimensionArgument(c, "dimension"), c.getArgument("filters", List.class))).build();

        argEntityType.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);

        argOutputFormat.addChild(argAreaTypeAll);
        argAreaTypeAll.addChild(argDimensionAll);
        argDimensionAll.addChild(argNamesAll);

        argOutputFormat.addChild(argAreaTypeInArea);
        argAreaTypeInArea.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimensionInArea);
        argDimensionInArea.addChild(argNamesBox);

        argOutputFormat.addChild(argAreaTypeInChunk);
        argAreaTypeInChunk.addChild(argChunkCoords);
        argChunkCoords.addChild(argDimensionInChunk);
        argDimensionInChunk.addChild(argNamesChunk);

        return argEntityType;
    }

    private static int dumpEntityData(LocateType target, AreaType areaType,
                                      CommandContext<CommandSource> context,
                                      IWorldRetriever dimensionGetter) throws CommandSyntaxException
    {
        return dumpEntityData(target, areaType, context, dimensionGetter, Collections.emptyList());
    }

    private static int dumpEntityData(LocateType target, AreaType areaType,
                                      CommandContext<CommandSource> context,
                                      IWorldRetriever dimensionGetter,
                                      List<String> filters) throws CommandSyntaxException
    {
        CommandSource source = context.getSource();
        World world = dimensionGetter.getWorldFromSource(source);

        OutputType outputType = context.getArgument("output_type", OutputType.class);
        DataDump.Format outputFormat = context.getArgument("output_format", DataDump.Format.class);

        ChunkProcessorBase processor = null;

        if (target == LocateType.TILE_ENTITY)
        {
            processor = new ChunkProcessorBlockEntityDataDumper(outputFormat, filters);
        }
        else if (target == LocateType.ENTITY)
        {
            processor = new ChunkProcessorEntityDataDumper(outputFormat, filters);
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
                    Vector2f vec = CommandUtils.getVec2fFromArg(context, "chunk");
                    ChunkPos pos = CommandUtils.getAsChunkPos(vec);
                    processor.processChunksInArea(world, pos, pos);
                    break;
                }

                case AREA:
                {
                    Vector3d vecStart = CommandUtils.getVec3dFromArg(context, "start_corner");
                    Vector3d vecEnd = CommandUtils.getVec3dFromArg(context, "end_corner");
                    ChunkPos pos1 = CommandUtils.getMinCornerChunkPos(vecStart, vecEnd);
                    ChunkPos pos2 = CommandUtils.getMaxCornerChunkPos(vecStart, vecEnd);
                    processor.setBoxCorners(vecStart, vecEnd);
                    processor.processChunksInArea(world, pos1, pos2);
                    break;
                }
            }

            DataDump dump = processor.getDump();
            dump.addHeader(0, String.format("Dimension: '%s'", WorldUtils.getDimensionId(world)));

            List<String> lines = dump.getLines();

            if (lines != null)
            {
                OutputUtils.printOutput(lines, outputType, outputFormat, "entity-data_" + target.getArgument(), source);
            }
        }

        return 1;
    }
}
