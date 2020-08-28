package fi.dy.masa.tellme.command;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec2Argument;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import fi.dy.masa.tellme.command.CommandUtils.AreaType;
import fi.dy.masa.tellme.command.CommandUtils.IWorldRetriever;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.chunkprocessor.BiomeStats;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandBiomeStats
{
    private static final Map<UUID, BiomeStats> BIOME_STATS = Maps.newHashMap();
    private static BiomeStats consoleBiomeStats;

    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("biome-stats").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createCountNodes("count", false));
        subCommandRootNode.addChild(createCountNodes("count-append", true));
        subCommandRootNode.addChild(createOutputDataNodes());

        return subCommandRootNode;
    }

    private static LiteralCommandNode<CommandSource> createCountNodes(String command, boolean isAppend)
    {
        LiteralCommandNode<CommandSource> actionNodeCount = Commands.literal(command).build();

        actionNodeCount.addChild(createCountNodeArea(isAppend));
        actionNodeCount.addChild(createCountNodeChunkRadius(isAppend));
        actionNodeCount.addChild(createCountNodeRange(isAppend));
        actionNodeCount.addChild(createCountNodeSampled(isAppend));

        return actionNodeCount;
    }

    private static LiteralCommandNode<CommandSource> createOutputDataNodes()
    {
        LiteralCommandNode<CommandSource> actionNodeOutputData = Commands.literal("output-data").build();

        ArgumentCommandNode<CommandSource, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        DataDump.Format.ASCII))
                .build();

        ArgumentCommandNode<CommandSource, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        c.getArgument("output_format", DataDump.Format.class)))
                .build();

        actionNodeOutputData.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);

        return actionNodeOutputData;
    }

    private static LiteralCommandNode<CommandSource> createCountNodeArea(boolean isAppend)
    {
        LiteralCommandNode<CommandSource> argAreaType = Commands.literal(AreaType.AREA.getArgument()).build();

        ArgumentCommandNode<CommandSource, ILocationArgument> argStartCorner = Commands.argument("start_corner", Vec2Argument.vec2()).build();
        ArgumentCommandNode<CommandSource, ILocationArgument> argEndCorner = Commands.argument("end_corner", Vec2Argument.vec2())
                .executes(c -> countBiomesArea(c.getSource(),
                        Vec2Argument.getVec2f(c, "start_corner"),
                        Vec2Argument.getVec2f(c, "end_corner"),
                        CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSource, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> countBiomesArea(c.getSource(),
                        Vec2Argument.getVec2f(c, "start_corner"),
                        Vec2Argument.getVec2f(c, "end_corner"),
                        (s) -> DimensionArgument.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSource> createCountNodeChunkRadius(boolean isAppend)
    {
        LiteralCommandNode<CommandSource> argAreaType = Commands.literal(AreaType.CHUNK_RADIUS.getArgument()).build();

        ArgumentCommandNode<CommandSource, Integer> argChunkRadius = Commands.argument("chunk_radius", IntegerArgumentType.integer(0, 4096))
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                                                      IntegerArgumentType.getInteger(c, "chunk_radius"),
                                                      CommandUtils.getVec2fFromSource(c.getSource()),
                                                      CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSource, ILocationArgument> argCenter = Commands.argument("center", Vec2Argument.vec2())
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                                                      IntegerArgumentType.getInteger(c, "chunk_radius"),
                                                      CommandUtils.getVec2fFromArg(c, "center"),
                                                      CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSource, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                        IntegerArgumentType.getInteger(c, "chunk_radius"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgument.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argChunkRadius);
        argChunkRadius.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSource> createCountNodeRange(boolean isAppend)
    {
        LiteralCommandNode<CommandSource> argAreaType = Commands.literal(AreaType.RANGE.getArgument()).build();

        ArgumentCommandNode<CommandSource, Integer> argChunkRadiusX = Commands.argument("range_x", IntegerArgumentType.integer(0, 16384)).build();
        ArgumentCommandNode<CommandSource, Integer> argChunkRadiusZ = Commands.argument("range_z", IntegerArgumentType.integer(0, 16384))
                .executes(c -> countBiomesRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "range_x"),
                                                IntegerArgumentType.getInteger(c, "range_z"),
                                                CommandUtils.getVec2fFromSource(c.getSource()),
                                                CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSource, ILocationArgument> argCenter = Commands.argument("center", Vec2Argument.vec2())
                .executes(c -> countBiomesRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "range_x"),
                                                IntegerArgumentType.getInteger(c, "range_z"),
                                                CommandUtils.getVec2fFromArg(c, "center"),
                                                CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSource, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> countBiomesRange(c.getSource(),
                        IntegerArgumentType.getInteger(c, "range_x"),
                        IntegerArgumentType.getInteger(c, "range_z"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgument.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argChunkRadiusX);
        argChunkRadiusX.addChild(argChunkRadiusZ);
        argChunkRadiusZ.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSource> createCountNodeSampled(boolean isAppend)
    {
        LiteralCommandNode<CommandSource> argAreaType = Commands.literal(AreaType.SAMPLED.getArgument()).build();

        ArgumentCommandNode<CommandSource, Integer> argSampleInterval = Commands.argument("sample_interval", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).build();
        ArgumentCommandNode<CommandSource, Integer> argSampleRadius   = Commands.argument("sample_radius",   IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                .executes(c -> countBiomesSampled(c.getSource(),
                                                  IntegerArgumentType.getInteger(c, "sample_interval"),
                                                  IntegerArgumentType.getInteger(c, "sample_radius"),
                                                  CommandUtils.getVec2fFromSource(c.getSource()),
                                                  CommandUtils::getWorldFromCommandSource, isAppend))
                .build();

        ArgumentCommandNode<CommandSource, ILocationArgument> argCenter = Commands.argument("center", Vec2Argument.vec2())
                .executes(c -> countBiomesSampled(c.getSource(),
                                                  IntegerArgumentType.getInteger(c, "sample_interval"),
                                                  IntegerArgumentType.getInteger(c, "sample_radius"),
                                                  CommandUtils.getVec2fFromArg(c, "center"),
                                                  CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSource, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.getDimension())
                .executes(c -> countBiomesSampled(c.getSource(),
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgument.getDimensionArgument(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argSampleInterval);
        argSampleInterval.addChild(argSampleRadius);
        argSampleRadius.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static int printHelp(CommandSource source)
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

    private static int countBiomesChunkRadius(CommandSource source, int chunkRadius, Vector2f center,
                                              IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        int centerChunkX = MathHelper.floor(center.x) >> 4;
        int centerChunkZ = MathHelper.floor(center.y) >> 4;
        BlockPos minPos = new BlockPos((centerChunkX - chunkRadius) * 16     , 0, (centerChunkZ - chunkRadius) * 16     );
        BlockPos maxPos = new BlockPos((centerChunkX + chunkRadius) * 16 + 15, 0, (centerChunkZ + chunkRadius) * 16 + 15);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesRange(CommandSource source, int rangeX, int rangeZ, Vector2f center,
                                        IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        int centerX = MathHelper.floor(center.x);
        int centerZ = MathHelper.floor(center.y);
        BlockPos minPos = new BlockPos(centerX - rangeX, 0, centerZ - rangeZ);
        BlockPos maxPos = new BlockPos(centerX + rangeX, 0, centerZ + rangeZ);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesArea(CommandSource source, Vector2f minPosVec2, Vector2f maxPosVec2,
                                       IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        BlockPos minPos = CommandUtils.getMinCorner(minPosVec2, maxPosVec2);
        BlockPos maxPos = CommandUtils.getMaxCorner(minPosVec2, maxPosVec2);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesArea(CommandSource source, BlockPos minPos, BlockPos maxPos,
                                       IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        World world = dimensionGetter.getWorldFromSource(source);
        BiomeStats biomeStats = getBiomeStatsFor(source, source.getEntity());
        BiomeManager biomeManager = world.getBiomeManager();

        CommandUtils.sendMessage(source, "Counting biomes...");

        biomeStats.setAppend(isAppend);
        biomeStats.getFullBiomeDistribution(biomeManager, minPos, maxPos);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int countBiomesSampled(CommandSource source, int sampleInterval,
                                          int sampleRadius, Vector2f center, IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        World world = dimensionGetter.getWorldFromSource(source);
        BiomeStats biomeStats = getBiomeStatsFor(source, source.getEntity());
        BiomeManager biomeManager = world.getBiomeManager();

        CommandUtils.sendMessage(source, "Counting biomes...");

        biomeStats.setAppend(isAppend);
        biomeStats.getSampledBiomeDistribution(biomeManager, (int) center.x, (int) center.y, sampleInterval, sampleRadius);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int outputData(CommandSource source, OutputType outputType, DataDump.Format format)
    {
        BiomeStats biomeStats = getBiomeStatsFor(source, source.getEntity());
        List<String> lines = biomeStats.queryAll(format);
        OutputUtils.printOutput(lines, outputType, format, "biome_stats", source);

        return 1;
    }

    private static BiomeStats getBiomeStatsFor(final CommandSource source, @Nullable Entity entity)
    {
        if (entity == null)
        {
            if (consoleBiomeStats == null)
            {
                consoleBiomeStats = new BiomeStats(source.func_241861_q().func_243612_b(Registry.BIOME_KEY));
            }

            return consoleBiomeStats;
        }

        return BIOME_STATS.computeIfAbsent(entity.getUniqueID(), (e) -> new BiomeStats(source.func_241861_q().func_243612_b(Registry.BIOME_KEY)));
    }
}
