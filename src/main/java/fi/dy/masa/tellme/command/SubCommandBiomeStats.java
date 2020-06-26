package fi.dy.masa.tellme.command;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.command.arguments.Vec2ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import fi.dy.masa.tellme.command.CommandUtils.AreaType;
import fi.dy.masa.tellme.command.CommandUtils.IWorldRetriever;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.chunkprocessor.BiomeStats;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandBiomeStats
{
    private static final Map<UUID, BiomeStats> BIOME_STATS = Maps.newHashMap();
    private static final BiomeStats CONSOLE_BIOME_STATS = new BiomeStats();

    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("biome-stats").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createCountNodes("count", false));
        subCommandRootNode.addChild(createCountNodes("count-append", true));
        subCommandRootNode.addChild(createOutputDataNodes());

        return subCommandRootNode;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodes(String command, boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> actionNodeCount = CommandManager.literal(command).build();

        actionNodeCount.addChild(createCountNodeArea(isAppend));
        actionNodeCount.addChild(createCountNodeChunkRadius(isAppend));
        actionNodeCount.addChild(createCountNodeRange(isAppend));
        actionNodeCount.addChild(createCountNodeSampled(isAppend));

        return actionNodeCount;
    }

    private static LiteralCommandNode<ServerCommandSource> createOutputDataNodes()
    {
        LiteralCommandNode<ServerCommandSource> actionNodeOutputData = CommandManager.literal("output-data").build();

        ArgumentCommandNode<ServerCommandSource, OutputType> argOutputType = CommandManager.argument("output_type", OutputTypeArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        DataDump.Format.ASCII))
                .build();

        ArgumentCommandNode<ServerCommandSource, DataDump.Format> argOutputFormat = CommandManager.argument("output_format", OutputFormatArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        c.getArgument("output_format", DataDump.Format.class)))
                .build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<ServerCommandSource, List<String>> argBiomeFilters = CommandManager.argument("biome_filters",
                StringCollectionArgument.create(() -> Registry.BIOME.getIds().stream().map(Identifier::toString).collect(Collectors.toList()), ""))
                .executes(ctx -> outputData(ctx.getSource(),
                        ctx.getArgument("output_type", OutputType.class),
                        ctx.getArgument("output_format", DataDump.Format.class),
                        ctx.getArgument("biome_filters", List.class)))
                .build();

        actionNodeOutputData.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);
        argOutputFormat.addChild(argBiomeFilters);

        return actionNodeOutputData;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodeArea(boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> argAreaType = CommandManager.literal(AreaType.AREA.getArgument()).build();

        ArgumentCommandNode<ServerCommandSource, PosArgument> argStartCorner = CommandManager.argument("start_corner", Vec2ArgumentType.vec2()).build();
        ArgumentCommandNode<ServerCommandSource, PosArgument> argEndCorner = CommandManager.argument("end_corner", Vec2ArgumentType.vec2())
                .executes(c -> countBiomesArea(c.getSource(),
                                               Vec2ArgumentType.getVec2(c, "start_corner"),
                                               Vec2ArgumentType.getVec2(c, "end_corner"),
                                               CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, Identifier> argDimension  = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> countBiomesArea(c.getSource(),
                    Vec2ArgumentType.getVec2(c, "start_corner"),
                    Vec2ArgumentType.getVec2(c, "end_corner"),
                    (s) -> DimensionArgumentType.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodeChunkRadius(boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> argAreaType = CommandManager.literal(AreaType.CHUNK_RADIUS.getArgument()).build();

        ArgumentCommandNode<ServerCommandSource, Integer> argChunkRadius = CommandManager.argument("chunk_radius", IntegerArgumentType.integer(0, 4096))
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                                                      IntegerArgumentType.getInteger(c, "chunk_radius"),
                                                      CommandUtils.getVec2fFromSource(c.getSource()),
                                                      CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, PosArgument> argCenter = CommandManager.argument("center", Vec2ArgumentType.vec2())
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                                                      IntegerArgumentType.getInteger(c, "chunk_radius"),
                                                      CommandUtils.getVec2fFromArg(c, "center"),
                                                      CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, Identifier> argDimension  = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                        IntegerArgumentType.getInteger(c, "chunk_radius"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgumentType.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argChunkRadius);
        argChunkRadius.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodeRange(boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> argAreaType = CommandManager.literal(AreaType.RANGE.getArgument()).build();

        ArgumentCommandNode<ServerCommandSource, Integer> argChunkRadiusX = CommandManager.argument("range_x", IntegerArgumentType.integer(0, 16384)).build();
        ArgumentCommandNode<ServerCommandSource, Integer> argChunkRadiusZ = CommandManager.argument("range_z", IntegerArgumentType.integer(0, 16384))
                .executes(c -> countBiomesRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "range_x"),
                                                IntegerArgumentType.getInteger(c, "range_z"),
                                                CommandUtils.getVec2fFromSource(c.getSource()),
                                                CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, PosArgument> argCenter = CommandManager.argument("center", Vec2ArgumentType.vec2())
                .executes(c -> countBiomesRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "range_x"),
                                                IntegerArgumentType.getInteger(c, "range_z"),
                                                CommandUtils.getVec2fFromArg(c, "center"),
                                                CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, Identifier> argDimension  = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> countBiomesRange(c.getSource(),
                        IntegerArgumentType.getInteger(c, "range_x"),
                        IntegerArgumentType.getInteger(c, "range_z"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgumentType.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argChunkRadiusX);
        argChunkRadiusX.addChild(argChunkRadiusZ);
        argChunkRadiusZ.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<ServerCommandSource> createCountNodeSampled(boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> argAreaType = CommandManager.literal(AreaType.SAMPLED.getArgument()).build();

        ArgumentCommandNode<ServerCommandSource, Integer> argSampleInterval = CommandManager.argument("sample_interval", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).build();
        ArgumentCommandNode<ServerCommandSource, Integer> argSampleRadius   = CommandManager.argument("sample_radius",   IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                .executes(c -> countBiomesSampled(c.getSource(),
                                                  IntegerArgumentType.getInteger(c, "sample_interval"),
                                                  IntegerArgumentType.getInteger(c, "sample_radius"),
                                                  CommandUtils.getVec2fFromSource(c.getSource()),
                                                  CommandUtils::getWorldFromCommandSource, isAppend))
                .build();

        ArgumentCommandNode<ServerCommandSource, PosArgument> argCenter = CommandManager.argument("center", Vec2ArgumentType.vec2())
                .executes(c -> countBiomesSampled(c.getSource(),
                                                  IntegerArgumentType.getInteger(c, "sample_interval"),
                                                  IntegerArgumentType.getInteger(c, "sample_radius"),
                                                  CommandUtils.getVec2fFromArg(c, "center"),
                                                  CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<ServerCommandSource, Identifier> argDimension  = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> countBiomesSampled(c.getSource(),
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgumentType.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argSampleInterval);
        argSampleInterval.addChild(argSampleRadius);
        argSampleRadius.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static int printHelp(ServerCommandSource source)
    {
        CommandUtils.sendMessage(source, "Calculates the number of x/z columns with each biome in a given area");
        CommandUtils.sendMessage(source, "Usage: /tellme biome-stats <count | count-append> area <x1> <z1> <x2> <z2> [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme biome-stats <count | count-append> chunk-radius <radius> [x z (of the center)] [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme biome-stats <count | count-append> range <x-distance> <z-distance> [x z (of the center)] [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme biome-stats <count | count-append> sampled <sample_interval> <sample_radius> [centerX centerZ] [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme biome-stats output-data <to-chat | to-console | to-file> <ascii | csv> [modid:biome] [modid:biome] ...");
        CommandUtils.sendMessage(source, "- count: Clears previously stored results, and then counts all the biomes in the given area");
        CommandUtils.sendMessage(source, "- count-append: Counts all the biomes in the given area, appending the data to the previously stored results");
        CommandUtils.sendMessage(source, "  > The sample_interval is the distance between sample points, in blocks");
        CommandUtils.sendMessage(source, "  > The sample_radius is how many sample points are checked per side/axis");
        CommandUtils.sendMessage(source, "- output-data: Outputs the stored data from previous count operations to the selected output location.");
        CommandUtils.sendMessage(source, "- output-data: The 'file' output's dump files will go to 'config/tellme/'.");
        CommandUtils.sendMessage(source, "- output-data: If you give some biome names, then only the data for those given biomes will be included in the output");

        return 1;
    }

    private static int countBiomesChunkRadius(ServerCommandSource source, int chunkRadius, Vec2f center,
                                              IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        int centerChunkX = MathHelper.floor(center.x) >> 4;
        int centerChunkZ = MathHelper.floor(center.y) >> 4;
        BlockPos minPos = new BlockPos((centerChunkX - chunkRadius) * 16     , 0, (centerChunkZ - chunkRadius) * 16     );
        BlockPos maxPos = new BlockPos((centerChunkX + chunkRadius) * 16 + 15, 0, (centerChunkZ + chunkRadius) * 16 + 15);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesRange(ServerCommandSource source, int rangeX, int rangeZ, Vec2f center,
                                        IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        int centerX = MathHelper.floor(center.x);
        int centerZ = MathHelper.floor(center.y);
        BlockPos minPos = new BlockPos(centerX - rangeX, 0, centerZ - rangeZ);
        BlockPos maxPos = new BlockPos(centerX + rangeX, 0, centerZ + rangeZ);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesArea(ServerCommandSource source, Vec2f minPosVec2, Vec2f maxPosVec2,
                                       IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        BlockPos minPos = CommandUtils.getMinCorner(minPosVec2, maxPosVec2);
        BlockPos maxPos = CommandUtils.getMaxCorner(minPosVec2, maxPosVec2);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesArea(ServerCommandSource source, BlockPos minPos, BlockPos maxPos,
                                       IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        World world = dimensionGetter.getWorldFromSource(source);
        BiomeStats biomeStats = getBiomeStatsFor(source.getEntity());
        BiomeAccess biomeAccess = world.getBiomeAccess();

        CommandUtils.sendMessage(source, "Counting biomes...");

        biomeStats.setAppend(isAppend);
        biomeStats.getFullBiomeDistribution(biomeAccess, minPos, maxPos);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int countBiomesSampled(ServerCommandSource source, int sampleInterval,
                                          int sampleRadius, Vec2f center, IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        World world = dimensionGetter.getWorldFromSource(source);
        BiomeStats biomeStats = getBiomeStatsFor(source.getEntity());
        BiomeAccess biomeAccess = world.getBiomeAccess();

        CommandUtils.sendMessage(source, "Counting biomes...");

        biomeStats.setAppend(isAppend);
        biomeStats.getSampledBiomeDistribution(biomeAccess, (int) center.x, (int) center.y, sampleInterval, sampleRadius);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int outputData(ServerCommandSource source, OutputType outputType, DataDump.Format format)
    {
        return outputData(source, outputType, format, null);
    }

    private static int outputData(ServerCommandSource source, OutputType outputType, DataDump.Format format, @Nullable List<String> filters)
    {
        BiomeStats biomeStats = getBiomeStatsFor(source.getEntity());
        List<String> lines;

        // We have some filters specified
        if (filters != null && filters.isEmpty() == false)
        {
            lines = biomeStats.query(format, filters);
        }
        else
        {
            lines = biomeStats.queryAll(format);
        }

        OutputUtils.printOutput(lines, outputType, format, "biome_stats", source);

        return 1;
    }

    private static BiomeStats getBiomeStatsFor(@Nullable Entity entity)
    {
        if (entity == null)
        {
            return CONSOLE_BIOME_STATS;
        }

        return BIOME_STATS.computeIfAbsent(entity.getUuid(), (e) -> new BiomeStats());
    }
}
