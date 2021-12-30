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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
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
    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("entity-data-dump").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createNodesEntities(LocateType.ENTITY));
        subCommandRootNode.addChild(createNodesEntities(LocateType.TILE_ENTITY));

        return subCommandRootNode;
    }

    private static int printHelp(CommandSourceStack source)
    {
        CommandUtils.sendMessage(source, "Dumps the NBT data of entities or TileEntities");
        CommandUtils.sendMessage(source, "Usage: /tellme entity-data-dump <entities | tile-entities> <to-chat | to-console | to-file> <ascii | csv> all-loaded [dimension] [name name ...]");
        CommandUtils.sendMessage(source, "Usage: /tellme entity-data-dump <entities | tile-entities> <to-chat | to-console | to-file> <ascii | csv> in-box <x1> <y1> <z1> <x2> <y2> <z2> [dimension] [name name ...]");
        CommandUtils.sendMessage(source, "Usage: /tellme entity-data-dump <entities | tile-entities> <to-chat | to-console | to-file> <ascii | csv> in-chunk <chunkX> <chunkZ> [dimension] [name name ...]");

        return 1;
    }

    private static LiteralCommandNode<CommandSourceStack> createNodesEntities(LocateType target)
    {
        LiteralCommandNode<CommandSourceStack> argEntityType = Commands.literal(target.getArgument()).build();
        ArgumentCommandNode<CommandSourceStack, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<CommandSourceStack, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create()).build();

        // all
        LiteralCommandNode<CommandSourceStack> argAreaTypeAll = Commands.literal(AreaType.ALL_LOADED.getArgument())
                .executes(c -> dumpEntityData(target, AreaType.ALL_LOADED, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimensionAll = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> dumpEntityData(target, AreaType.ALL_LOADED, c, (s) -> DimensionArgument.getDimension(c, "dimension"))).build();

        // in-box
        LiteralCommandNode<CommandSourceStack> argAreaTypeInArea = Commands.literal("box")
                .executes(c -> dumpEntityData(target, AreaType.AREA, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argStartCorner = Commands.argument("start_corner", Vec3Argument.vec3()).build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argEndCorner = Commands.argument("end_corner", Vec3Argument.vec3())
                .executes(c -> dumpEntityData(target, AreaType.AREA, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimensionInArea = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> dumpEntityData(target, AreaType.AREA, c, (s) -> DimensionArgument.getDimension(c, "dimension"))).build();

        // in-chunk
        LiteralCommandNode<CommandSourceStack> argAreaTypeInChunk = Commands.literal(AreaType.CHUNK.getArgument())
                .executes(c -> dumpEntityData(target, AreaType.CHUNK, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argChunkCoords = Commands.argument("chunk", Vec2Argument.vec2())
                .executes(c -> dumpEntityData(target, AreaType.CHUNK, c, CommandUtils::getWorldFromCommandSource)).build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimensionInChunk = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> dumpEntityData(target, AreaType.CHUNK, c, (s) -> DimensionArgument.getDimension(c, "dimension"))).build();

        ArgumentCommandNode<CommandSourceStack, List<String>> argNamesAll = Commands.argument("filters",
                StringCollectionArgument.create(() -> target.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(c -> dumpEntityData(target, AreaType.ALL_LOADED, c, (s) -> DimensionArgument.getDimension(c, "dimension"), c.getArgument("filters", List.class))).build();

        ArgumentCommandNode<CommandSourceStack, List<String>> argNamesBox = Commands.argument("filters",
                StringCollectionArgument.create(() -> target.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(c -> dumpEntityData(target, AreaType.AREA, c, (s) -> DimensionArgument.getDimension(c, "dimension"), c.getArgument("filters", List.class))).build();

        ArgumentCommandNode<CommandSourceStack, List<String>> argNamesChunk = Commands.argument("filters",
                StringCollectionArgument.create(() -> target.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(c -> dumpEntityData(target, AreaType.CHUNK, c, (s) -> DimensionArgument.getDimension(c, "dimension"), c.getArgument("filters", List.class))).build();

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
                                      CommandContext<CommandSourceStack> context,
                                      IWorldRetriever dimensionGetter) throws CommandSyntaxException
    {
        return dumpEntityData(target, areaType, context, dimensionGetter, Collections.emptyList());
    }

    private static int dumpEntityData(LocateType target, AreaType areaType,
                                      CommandContext<CommandSourceStack> context,
                                      IWorldRetriever dimensionGetter,
                                      List<String> filters) throws CommandSyntaxException
    {
        CommandSourceStack source = context.getSource();
        Level world = dimensionGetter.getWorldFromSource(source);

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
                    Vec2 vec = CommandUtils.getVec2fFromArg(context, "chunk");
                    ChunkPos pos = CommandUtils.getAsChunkPos(vec);
                    processor.processChunksInArea(world, pos, pos);
                    break;
                }

                case AREA:
                {
                    Vec3 vecStart = CommandUtils.getVec3dFromArg(context, "start_corner");
                    Vec3 vecEnd = CommandUtils.getVec3dFromArg(context, "end_corner");
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
