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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
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

    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("block-stats").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createCountNodes("count", false));
        subCommandRootNode.addChild(createCountNodes("count-append", true));
        subCommandRootNode.addChild(createOutputDataNodes());

        return subCommandRootNode;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodes(String command, boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> actionNodeCount = Commands.literal(command).build();

        actionNodeCount.addChild(createCountNodeAllLoadedChunks(isAppend));
        actionNodeCount.addChild(createCountNodeArea(isAppend));
        actionNodeCount.addChild(createCountNodeBox(isAppend));
        actionNodeCount.addChild(createCountNodeRange(isAppend));

        return actionNodeCount;
    }

    // tellme output-data <to-chat | to-console | to-file> <ascii | csv> [sort-by-count] [modid:block] [modid:block] ...
    private static LiteralCommandNode<CommandSourceStack> createOutputDataNodes()
    {
        LiteralCommandNode<CommandSourceStack> actionNodeOutputData = Commands.literal("output-data").build();

        ArgumentCommandNode<CommandSourceStack, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create()).build();

        ArgumentCommandNode<CommandSourceStack, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create()).build();

        ArgumentCommandNode<CommandSourceStack, CommandUtils.BlockStateGrouping> argDataGrouping = Commands.argument("result_grouping", BlockStateCountGroupingArgument.create())
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          false))
                .build();

        LiteralCommandNode<CommandSourceStack> argSortByCount = Commands.literal("sort-by-count")
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          true))
                .build();

        LiteralCommandNode<CommandSourceStack> argSortByName = Commands.literal("sort-by-name")
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          false))
                .build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<CommandSourceStack, List<String>> argBlockFiltersSortByCount = Commands.argument("block_filters",
                StringCollectionArgument.create(() -> ForgeRegistries.BLOCKS.getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(c -> outputData(c.getSource(),
                                          c.getArgument("output_type", OutputType.class),
                                          c.getArgument("output_format", DataDump.Format.class),
                                          c.getArgument("result_grouping", CommandUtils.BlockStateGrouping.class),
                                          true,
                                          c.getArgument("block_filters", List.class)))
                .build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<CommandSourceStack, List<String>> argBlockFiltersSortByName = Commands.argument("block_filters",
                StringCollectionArgument.create(() -> ForgeRegistries.BLOCKS.getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
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

    private static LiteralCommandNode<CommandSourceStack> createCountNodeAllLoadedChunks(boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> argAreaType = Commands.literal(AreaType.LOADED.getArgument())
                .executes(c -> countBlocksLoadedChunks(c.getSource(),
                                                       CommandUtils::getWorldFromCommandSource, isAppend)).build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> countBlocksLoadedChunks(c.getSource(),
                          (s) -> DimensionArgument.getDimension(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodeArea(boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> argAreaType = Commands.literal(AreaType.AREA.getArgument()).build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argStartCorner = Commands.argument("start_corner", Vec2Argument.vec2()).build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argEndCorner = Commands.argument("end_corner", Vec2Argument.vec2())
                .executes(c -> countBlocksArea(c.getSource(),
                        Vec2Argument.getVec2(c, "start_corner"),
                        Vec2Argument.getVec2(c, "end_corner"),
                        CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> countBlocksArea(c.getSource(),
                        Vec2Argument.getVec2(c, "start_corner"),
                        Vec2Argument.getVec2(c, "end_corner"),
                        (s) -> DimensionArgument.getDimension(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodeBox(boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> argAreaType = Commands.literal(AreaType.BOX.getArgument()).build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argStartCorner = Commands.argument("start_corner", Vec3Argument.vec3()).build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argEndCorner = Commands.argument("end_corner", Vec3Argument.vec3())
                .executes(c -> countBlocksBox(c.getSource(),
                        Vec3Argument.getVec3(c, "start_corner"),
                        Vec3Argument.getVec3(c, "end_corner"),
                        CommandUtils::getWorldFromCommandSource, isAppend))
                .build();

        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> countBlocksBox(c.getSource(),
                        Vec3Argument.getVec3(c, "start_corner"),
                        Vec3Argument.getVec3(c, "end_corner"),
                        (s) -> DimensionArgument.getDimension(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodeRange(boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> argAreaType = Commands.literal(AreaType.RANGE.getArgument()).build();

        ArgumentCommandNode<CommandSourceStack, Integer> argChunkBlockRange = Commands.argument("block_range", IntegerArgumentType.integer(0, 8192))
                .executes(c -> countBlocksRange(c.getSource(),
                        IntegerArgumentType.getInteger(c, "block_range"),
                        CommandUtils.getVec3dFromSource(c.getSource()),
                        CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argCenter = Commands.argument("center", Vec3Argument.vec3())
                .executes(c -> countBlocksRange(c.getSource(),
                        IntegerArgumentType.getInteger(c, "block_range"),
                        CommandUtils.getVec3dFromArg(c, "center"),
                        CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> countBlocksRange(c.getSource(),
                        IntegerArgumentType.getInteger(c, "block_range"),
                        CommandUtils.getVec3dFromArg(c, "center"),
                        (s) -> DimensionArgument.getDimension(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argChunkBlockRange);
        argChunkBlockRange.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static int countBlocksRange(CommandSourceStack source, int range, Vec3 center,
                                        IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        BlockPos centerPos = new BlockPos(center);
        Level world = dimensionGetter.getWorldFromSource(source);
        int minY = world.getMinBuildHeight();
        int maxY = world.getMaxBuildHeight() - 1;
        BlockPos minPos = new BlockPos(centerPos.getX() - range, Math.max(minY, centerPos.getY() - range), centerPos.getZ() - range);
        BlockPos maxPos = new BlockPos(centerPos.getX() + range, Math.min(maxY, centerPos.getY() + range), centerPos.getZ() + range);

        return countBlocksBox(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBlocksBox(CommandSourceStack source, Vec3 corner1, Vec3 corner2,
                                      IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        BlockPos minPos = CommandUtils.getMinCorner(corner1, corner2);
        BlockPos maxPos = CommandUtils.getMaxCorner(corner1, corner2);

        return countBlocksBox(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBlocksArea(CommandSourceStack source, Vec2 corner1, Vec2 corner2,
                                       IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        Level world = dimensionGetter.getWorldFromSource(source);
        BlockPos minPos = CommandUtils.getMinCorner(corner1, corner2, world);
        BlockPos maxPos = CommandUtils.getMaxCorner(corner1, corner2, world);

        return countBlocksBox(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBlocksBox(CommandSourceStack source, BlockPos minPos, BlockPos maxPos,
                                      IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        Level world = dimensionGetter.getWorldFromSource(source);
        BlockStats blockStats = getBlockStatsFor(source.getEntity());

        CommandUtils.sendMessage(source, "Counting blocks...");

        blockStats.setAppend(isAppend);
        blockStats.processChunks(world, minPos, maxPos);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int countBlocksLoadedChunks(CommandSourceStack source, IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        Level world = dimensionGetter.getWorldFromSource(source);
        BlockStats blockStats = getBlockStatsFor(source.getEntity());

        CommandUtils.sendMessage(source, "Counting blocks...");

        blockStats.setAppend(isAppend);
        blockStats.processChunks(TellMe.dataProvider.getLoadedChunks(world), world);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int printHelp(CommandSourceStack source)
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

    private static int outputData(CommandSourceStack source, OutputType outputType, DataDump.Format format, CommandUtils.BlockStateGrouping grouping, boolean sortByCount) throws CommandSyntaxException
    {
        return outputData(source, outputType, format, grouping, sortByCount, null);
    }

    private static int outputData(CommandSourceStack source, OutputType outputType, DataDump.Format format, CommandUtils.BlockStateGrouping grouping, boolean sortByCount, @Nullable List<String> filters) throws CommandSyntaxException
    {
        BlockStats blockStats = getBlockStatsFor(source.getEntity());
        List<String> lines;

        // We have some filters specified
        if (filters != null && filters.isEmpty() == false)
        {
            lines = blockStats.query(format, grouping, sortByCount, filters);
        }
        else
        {
            lines = blockStats.queryAll(format, grouping, sortByCount);
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

        return BLOCK_STATS.computeIfAbsent(entity.getUUID(), (e) -> new BlockStats());
    }
}
