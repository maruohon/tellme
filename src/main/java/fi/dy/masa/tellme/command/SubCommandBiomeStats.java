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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec2;
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

    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("biome-stats").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createCountNodes("count", false));
        subCommandRootNode.addChild(createCountNodes("count-append", true));
        subCommandRootNode.addChild(createOutputDataNodes());

        return subCommandRootNode;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodes(String command, boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> actionNodeCount = Commands.literal(command).build();

        actionNodeCount.addChild(createCountNodeArea(isAppend));
        actionNodeCount.addChild(createCountNodeChunkRadius(isAppend));
        actionNodeCount.addChild(createCountNodeRange(isAppend));
        actionNodeCount.addChild(createCountNodeSampled(isAppend));

        return actionNodeCount;
    }

    private static LiteralCommandNode<CommandSourceStack> createOutputDataNodes()
    {
        LiteralCommandNode<CommandSourceStack> actionNodeOutputData = Commands.literal("output-data").build();

        ArgumentCommandNode<CommandSourceStack, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        DataDump.Format.ASCII))
                .build();

        ArgumentCommandNode<CommandSourceStack, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        c.getArgument("output_format", DataDump.Format.class)))
                .build();

        actionNodeOutputData.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);

        return actionNodeOutputData;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodeArea(boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> argAreaType = Commands.literal(AreaType.AREA.getArgument()).build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argStartCorner = Commands.argument("start_corner", Vec2Argument.vec2()).build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argEndCorner = Commands.argument("end_corner", Vec2Argument.vec2())
                .executes(c -> countBiomesArea(c.getSource(),
                        Vec2Argument.getVec2(c, "start_corner"),
                        Vec2Argument.getVec2(c, "end_corner"),
                        CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> countBiomesArea(c.getSource(),
                        Vec2Argument.getVec2(c, "start_corner"),
                        Vec2Argument.getVec2(c, "end_corner"),
                        (s) -> DimensionArgument.getDimension(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argStartCorner);
        argStartCorner.addChild(argEndCorner);
        argEndCorner.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodeChunkRadius(boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> argAreaType = Commands.literal(AreaType.CHUNK_RADIUS.getArgument()).build();

        ArgumentCommandNode<CommandSourceStack, Integer> argChunkRadius = Commands.argument("chunk_radius", IntegerArgumentType.integer(0, 4096))
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                                                      IntegerArgumentType.getInteger(c, "chunk_radius"),
                                                      CommandUtils.getVec2fFromSource(c.getSource()),
                                                      CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argCenter = Commands.argument("center", Vec2Argument.vec2())
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                                                      IntegerArgumentType.getInteger(c, "chunk_radius"),
                                                      CommandUtils.getVec2fFromArg(c, "center"),
                                                      CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> countBiomesChunkRadius(c.getSource(),
                        IntegerArgumentType.getInteger(c, "chunk_radius"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgument.getDimension(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argChunkRadius);
        argChunkRadius.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodeRange(boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> argAreaType = Commands.literal(AreaType.RANGE.getArgument()).build();

        ArgumentCommandNode<CommandSourceStack, Integer> argChunkRadiusX = Commands.argument("range_x", IntegerArgumentType.integer(0, 16384)).build();
        ArgumentCommandNode<CommandSourceStack, Integer> argChunkRadiusZ = Commands.argument("range_z", IntegerArgumentType.integer(0, 16384))
                .executes(c -> countBiomesRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "range_x"),
                                                IntegerArgumentType.getInteger(c, "range_z"),
                                                CommandUtils.getVec2fFromSource(c.getSource()),
                                                CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argCenter = Commands.argument("center", Vec2Argument.vec2())
                .executes(c -> countBiomesRange(c.getSource(),
                                                IntegerArgumentType.getInteger(c, "range_x"),
                                                IntegerArgumentType.getInteger(c, "range_z"),
                                                CommandUtils.getVec2fFromArg(c, "center"),
                                                CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> countBiomesRange(c.getSource(),
                        IntegerArgumentType.getInteger(c, "range_x"),
                        IntegerArgumentType.getInteger(c, "range_z"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgument.getDimension(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argChunkRadiusX);
        argChunkRadiusX.addChild(argChunkRadiusZ);
        argChunkRadiusZ.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static LiteralCommandNode<CommandSourceStack> createCountNodeSampled(boolean isAppend)
    {
        LiteralCommandNode<CommandSourceStack> argAreaType = Commands.literal(AreaType.SAMPLED.getArgument()).build();

        ArgumentCommandNode<CommandSourceStack, Integer> argSampleInterval = Commands.argument("sample_interval", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).build();
        ArgumentCommandNode<CommandSourceStack, Integer> argSampleRadius   = Commands.argument("sample_radius",   IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                .executes(c -> countBiomesSampled(c.getSource(),
                                                  IntegerArgumentType.getInteger(c, "sample_interval"),
                                                  IntegerArgumentType.getInteger(c, "sample_radius"),
                                                  CommandUtils.getVec2fFromSource(c.getSource()),
                                                  CommandUtils::getWorldFromCommandSource, isAppend))
                .build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argCenter = Commands.argument("center", Vec2Argument.vec2())
                .executes(c -> countBiomesSampled(c.getSource(),
                                                  IntegerArgumentType.getInteger(c, "sample_interval"),
                                                  IntegerArgumentType.getInteger(c, "sample_radius"),
                                                  CommandUtils.getVec2fFromArg(c, "center"),
                                                  CommandUtils::getWorldFromCommandSource, isAppend))
                .build();
        ArgumentCommandNode<CommandSourceStack, ResourceLocation> argDimension  = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> countBiomesSampled(c.getSource(),
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius"),
                        CommandUtils.getVec2fFromArg(c, "center"),
                        (s) -> DimensionArgument.getDimension(c, "dimension"), isAppend))
                .build();

        argAreaType.addChild(argSampleInterval);
        argSampleInterval.addChild(argSampleRadius);
        argSampleRadius.addChild(argCenter);
        argCenter.addChild(argDimension);

        return argAreaType;
    }

    private static int printHelp(CommandSourceStack source)
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

    private static int countBiomesChunkRadius(CommandSourceStack source, int chunkRadius, Vec2 center,
                                              IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        int centerChunkX = Mth.floor(center.x) >> 4;
        int centerChunkZ = Mth.floor(center.y) >> 4;
        BlockPos minPos = new BlockPos((centerChunkX - chunkRadius) * 16     , 0, (centerChunkZ - chunkRadius) * 16     );
        BlockPos maxPos = new BlockPos((centerChunkX + chunkRadius) * 16 + 15, 0, (centerChunkZ + chunkRadius) * 16 + 15);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesRange(CommandSourceStack source, int rangeX, int rangeZ, Vec2 center,
                                        IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        int centerX = Mth.floor(center.x);
        int centerZ = Mth.floor(center.y);
        BlockPos minPos = new BlockPos(centerX - rangeX, 0, centerZ - rangeZ);
        BlockPos maxPos = new BlockPos(centerX + rangeX, 0, centerZ + rangeZ);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesArea(CommandSourceStack source, Vec2 minPosVec2, Vec2 maxPosVec2,
                                       IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        BlockPos minPos = CommandUtils.getMinCorner(minPosVec2, maxPosVec2);
        BlockPos maxPos = CommandUtils.getMaxCorner(minPosVec2, maxPosVec2);

        return countBiomesArea(source, minPos, maxPos, dimensionGetter, isAppend);
    }

    private static int countBiomesArea(CommandSourceStack source, BlockPos minPos, BlockPos maxPos,
                                       IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        Level world = dimensionGetter.getWorldFromSource(source);
        BiomeStats biomeStats = getBiomeStatsFor(source, source.getEntity());
        BiomeManager biomeManager = world.getBiomeManager();

        CommandUtils.sendMessage(source, "Counting biomes...");

        biomeStats.setAppend(isAppend);
        biomeStats.getFullBiomeDistribution(biomeManager, minPos, maxPos);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int countBiomesSampled(CommandSourceStack source, int sampleInterval,
                                          int sampleRadius, Vec2 center, IWorldRetriever dimensionGetter, boolean isAppend) throws CommandSyntaxException
    {
        Level world = dimensionGetter.getWorldFromSource(source);
        BiomeStats biomeStats = getBiomeStatsFor(source, source.getEntity());
        BiomeManager biomeManager = world.getBiomeManager();

        CommandUtils.sendMessage(source, "Counting biomes...");

        biomeStats.setAppend(isAppend);
        biomeStats.getSampledBiomeDistribution(biomeManager, (int) center.x, (int) center.y, sampleInterval, sampleRadius);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int outputData(CommandSourceStack source, OutputType outputType, DataDump.Format format)
    {
        BiomeStats biomeStats = getBiomeStatsFor(source, source.getEntity());
        List<String> lines = biomeStats.queryAll(format);
        OutputUtils.printOutput(lines, outputType, format, "biome_stats", source);

        return 1;
    }

    private static BiomeStats getBiomeStatsFor(final CommandSourceStack source, @Nullable Entity entity)
    {
        if (entity == null)
        {
            if (consoleBiomeStats == null)
            {
                consoleBiomeStats = new BiomeStats(source.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
            }

            return consoleBiomeStats;
        }

        return BIOME_STATS.computeIfAbsent(entity.getUUID(), (e) -> new BiomeStats(source.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)));
    }
}
