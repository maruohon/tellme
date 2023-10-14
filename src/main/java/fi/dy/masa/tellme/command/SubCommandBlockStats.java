package fi.dy.masa.tellme.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.block.Block;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.AreaType;
import fi.dy.masa.tellme.command.CommandUtils.IWorldRetriever;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.BlockStateCountGroupingArgument;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.chunkprocessor.BlockStats;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandBlockStats
{
    private static final Map<UUID, BlockStats> BLOCK_STATS = new HashMap<>();
    private static final BlockStats CONSOLE_BLOCK_STATS = new BlockStats();

    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("block-stats").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createCountNodes("count", false));
        subCommandRootNode.addChild(createCountNodes("count-append", true));
        subCommandRootNode.addChild(createOutputDataNodes());

        return subCommandRootNode;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodes(String command, boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> actionNodeCount = CommandManager.literal(command).build();

        actionNodeCount.addChild(createCountNodeAllLoadedChunks(isAppend));
        actionNodeCount.addChild(createCountNodeArea(isAppend));
        actionNodeCount.addChild(createCountNodeBox(isAppend));
        actionNodeCount.addChild(createCountNodeRange(isAppend));

        return actionNodeCount;
    }

    // tellme output-data <to-chat | to-console | to-file> <ascii | csv> [sort-by-count] [modid:block] [modid:block] ...
    private static LiteralCommandNode<ServerCommandSource> createOutputDataNodes()
    {
        LiteralCommandNode<ServerCommandSource> actionNodeOutputData = CommandManager.literal("output-data").build();

        ArgumentCommandNode<ServerCommandSource, OutputType> argOutputType = CommandManager.argument("output_type", OutputTypeArgument.create()).build();

        ArgumentCommandNode<ServerCommandSource, DataDump.Format> argOutputFormat = CommandManager.argument("output_format", OutputFormatArgument.create()).build();

        ArgumentCommandNode<ServerCommandSource, CommandUtils.BlockStateGrouping> argDataGrouping = CommandManager.argument("result_grouping", BlockStateCountGroupingArgument.create())
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          false))
                .build();

        LiteralCommandNode<ServerCommandSource> argSortByCount = CommandManager.literal("sort-by-count")
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          true))
                .build();

        LiteralCommandNode<ServerCommandSource> argSortByName = CommandManager.literal("sort-by-name")
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          false))
                .build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<ServerCommandSource, List<String>> argBlockFiltersSortByCount = CommandManager.argument("block_filters",
                StringCollectionArgument.create(() -> Registries.BLOCK.getIds().stream().map(Identifier::toString).collect(Collectors.toList()), ""))
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          true,
                                          c.getArgument("block_filters", List.class)))
                .build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<ServerCommandSource, List<String>> argBlockFiltersSortByName = CommandManager.argument("block_filters",
                StringCollectionArgument.create(() -> Registries.BLOCK.getIds().stream().map(Identifier::toString).collect(Collectors.toList()), ""))
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          false,
                                          c.getArgument("block_filters", List.class)))
                .build();

        actionNodeOutputData.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);
        argOutputFormat.addChild(argDataGrouping);

        argDataGrouping.addChild(argSortByCount);
        argSortByCount.addChild(argBlockFiltersSortByCount);

        argDataGrouping.addChild(argSortByName);
        argSortByName.addChild(argBlockFiltersSortByName);

        return actionNodeOutputData;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodeAllLoadedChunks(boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> argAreaType = CommandManager.literal(AreaType.LOADED.getArgument())
                .executes(c -> countBlocksLoadedChunks(c.getSource(),
                                                       CommandUtils::getWorldFromCommandSource, isAppend)).build();

        ArgumentCommandNode<ServerCommandSource, Identifier> argDimension  = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> countBlocksLoadedChunks(c.getSource(),
                          (s) -> DimensionArgumentType.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodeArea(boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> argAreaType = CommandManager.literal(AreaType.AREA.getArgument()).build();

        ArgumentCommandNode<ServerCommandSource, PosArgument> argStartCorner = CommandManager.argument("start_corner", Vec2ArgumentType.vec2()).build();
        ArgumentCommandNode<ServerCommandSource, PosArgument> argEndCorner = CommandManager.argument("end_corner", Vec2ArgumentType.vec2())
                .executes(c -> countBlocksArea(c.getSource(),
                                               Vec2ArgumentType.getVec2(c, "start_corner"),
                                               Vec2ArgumentType.getVec2(c, "end_corner"),
                                               CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, Identifier> argDimension  = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> countBlocksArea(c.getSource(),
                                               Vec2ArgumentType.getVec2(c, "start_corner"),
                                               Vec2ArgumentType.getVec2(c, "end_corner"),
                                               (s) -> DimensionArgumentType.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodeBox(boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> argAreaType = CommandManager.literal(AreaType.BOX.getArgument()).build();

        ArgumentCommandNode<ServerCommandSource, PosArgument> argStartCorner = CommandManager.argument("start_corner", Vec3ArgumentType.vec3()).build();
        ArgumentCommandNode<ServerCommandSource, PosArgument> argEndCorner = CommandManager.argument("end_corner", Vec3ArgumentType.vec3())
                .executes(c -> countBlocksBox(c.getSource(),
                                              Vec3ArgumentType.getVec3(c, "start_corner"),
                                              Vec3ArgumentType.getVec3(c, "end_corner"),
                                              CommandUtils::getWorldFromCommandSource, isAppend))
                .build();

        ArgumentCommandNode<ServerCommandSource, Identifier> argDimension  = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> countBlocksBox(c.getSource(),
                                              Vec3ArgumentType.getVec3(c, "start_corner"),
                                              Vec3ArgumentType.getVec3(c, "end_corner"),
                                              (s) -> DimensionArgumentType.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodeRange(boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> argAreaType = CommandManager.literal(AreaType.RANGE.getArgument()).build();

        ArgumentCommandNode<ServerCommandSource, Integer> argChunkBlockRange = CommandManager.argument("block_range", IntegerArgumentType.integer(0, 8192))
                .executes(c -> countBlocksRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "block_range"),
                                                CommandUtils.getVec3dFromSource(c.getSource()),
                                                CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, PosArgument> argCenter = CommandManager.argument("center", Vec3ArgumentType.vec3())
                .executes(c -> countBlocksRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "block_range"),
                                                CommandUtils.getVec3dFromArg(c, "center"),
                                                CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, Identifier> argDimension  = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> countBlocksRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "block_range"),
                                                CommandUtils.getVec3dFromArg(c, "center"),
                                                (s) -> DimensionArgumentType.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argChunkBlockRange);
        argChunkBlockRange.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static int countBlocksRange(ServerCommandSource source, int range, Vec3d center,
                                        IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        BlockPos centerPos = BlockPos.ofFloored(center);
        World world = dimensionGetter.getWorldFromSource(source);
        int minY = world.getBottomY();
        int maxY = world.getTopY() - 1;
        BlockPos minPos = new BlockPos(centerPos.getX() - range, Math.max(minY, centerPos.getY() - range), centerPos.getZ() - range);
        BlockPos maxPos = new BlockPos(centerPos.getX() + range, Math.min(maxY, centerPos.getY() + range), centerPos.getZ() + range);

        return countBlocksBox(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBlocksBox(ServerCommandSource source, Vec3d corner1, Vec3d corner2,
                                      IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        BlockPos minPos = CommandUtils.getMinCorner(corner1, corner2);
        BlockPos maxPos = CommandUtils.getMaxCorner(corner1, corner2);

        return countBlocksBox(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBlocksArea(ServerCommandSource source, Vec2f corner1, Vec2f corner2,
                                       IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        World world = dimensionGetter.getWorldFromSource(source);
        BlockPos minPos = CommandUtils.getMinCorner(corner1, corner2, world);
        BlockPos maxPos = CommandUtils.getMaxCorner(corner1, corner2, world);

        return countBlocksBox(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBlocksBox(ServerCommandSource source, BlockPos minPos, BlockPos maxPos,
                                      IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        World world = dimensionGetter.getWorldFromSource(source);
        BlockStats blockStats = getBlockStatsFor(source.getEntity());

        CommandUtils.sendMessage(source, "Counting blocks...");

        blockStats.setAppend(isAppend);
        blockStats.processChunks(world, minPos, maxPos);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int countBlocksLoadedChunks(ServerCommandSource source, IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        World world = dimensionGetter.getWorldFromSource(source);
        BlockStats blockStats = getBlockStatsFor(source.getEntity());

        CommandUtils.sendMessage(source, "Counting blocks...");

        blockStats.setAppend(isAppend);
        blockStats.processChunks(TellMe.dataProvider.getLoadedChunks(world), world);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int printHelp(ServerCommandSource source)
    {
        CommandUtils.sendMessage(source, "Calculates the number of blocks in a given area");
        CommandUtils.sendMessage(source, "Usage: /tellme block-stats count[-append] all-loaded-chunks [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme block-stats count[-append] area <x1> <z1> <x2> <z2> [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme block-stats count[-append] box <x1> <y1> <z1> <x2> <y2> <z2> [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme block-stats count[-append] range <block_range> [x y z (of the center)] [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme block-stats output-data <to-chat | to-console | to-file> <ascii | csv> <by-block | by-state> [sort-by-count] [modid:block] [modid:block] ...");
        CommandUtils.sendMessage(source, "- count: Clears previously stored results, and then counts all the blocks in the given area");
        CommandUtils.sendMessage(source, "- count-append: Counts all the blocks in the given area, appending the data to the previously stored results");
        CommandUtils.sendMessage(source, "- output-data: Outputs the stored data from previous count operations to the selected output location.");
        CommandUtils.sendMessage(source, "- output-data: The 'file' output's dump files will go to 'config/tellme/'.");
        CommandUtils.sendMessage(source, "- output-data: If you give some block names, then only the data for those given blocks will be included in the output");

        return 1;
    }

    private static int outputData(ServerCommandSource source, OutputType outputType, DataDump.Format format, CommandUtils.BlockStateGrouping grouping, boolean sortByCount) throws CommandSyntaxException
    {
        return outputData(source, outputType, format, grouping, sortByCount, null);
    }

    private static int outputData(ServerCommandSource source, OutputType outputType,
                                  DataDump.Format format, CommandUtils.BlockStateGrouping grouping,
                                  boolean sortByCount, @Nullable List<String> filters) throws CommandSyntaxException
    {
        BlockStats blockStats = getBlockStatsFor(source.getEntity());
        RegistryWrapper<Block> registryWrapper = source.getWorld().createCommandRegistryWrapper(RegistryKeys.BLOCK);
        List<String> lines;

        // We have some filters specified
        if (filters != null && filters.isEmpty() == false)
        {
            lines = blockStats.query(format, grouping, sortByCount, filters, registryWrapper);
        }
        else
        {
            lines = blockStats.queryAll(format, grouping, sortByCount, registryWrapper);
        }

        OutputUtils.printOutput(lines, outputType, format, "block_stats", source);

        return 1;
    }

    private static BlockStats getBlockStatsFor(@Nullable Entity entity)
    {
        if (entity == null)
        {
            return CONSOLE_BLOCK_STATS;
        }

        return BLOCK_STATS.computeIfAbsent(entity.getUuid(), (e) -> new BlockStats());
    }
}
