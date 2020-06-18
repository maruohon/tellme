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
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.command.arguments.Vec2ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.util.BiomeLocator;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandBiomeLocate
{
    private static final Map<UUID, BiomeLocator> BIOME_LOCATORS = Maps.newHashMap();
    private static final BiomeLocator CONSOLE_BIOME_LOCATOR = new BiomeLocator();

    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("locate-biome").executes(c -> printHelp(c.getSource())).build();

        LiteralCommandNode<ServerCommandSource> actionNodeOutputData = CommandManager.literal("output-data").build();
        ArgumentCommandNode<ServerCommandSource, OutputType> argOutputType = CommandManager.argument("output_type", OutputTypeArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        DataDump.Format.ASCII))
                .build();
        ArgumentCommandNode<ServerCommandSource, DataDump.Format> argOutputFormat = CommandManager.argument("output_format", OutputFormatArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        c.getArgument("output_format", DataDump.Format.class)
                        ))
                .build();

        subCommandRootNode.addChild(createNodes("search", false));
        subCommandRootNode.addChild(createNodes("search-append", true));

        subCommandRootNode.addChild(actionNodeOutputData);
        actionNodeOutputData.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);

        return subCommandRootNode;
    }

    private static LiteralCommandNode<ServerCommandSource> createNodes(String command, boolean isAppend)
    {
        LiteralCommandNode<ServerCommandSource> actionNodeSearch = CommandManager.literal(command).build();

        ArgumentCommandNode<ServerCommandSource, Integer> argSampleInterval = CommandManager.argument("sample_interval", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).build();
        ArgumentCommandNode<ServerCommandSource, Integer> argSampleRadius = CommandManager.argument("sample_radius",   IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                .executes(c -> search(c.getSource(), isAppend,
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius")))
                .build();

        ArgumentCommandNode<ServerCommandSource, PosArgument> argCenter = CommandManager.argument("center", Vec2ArgumentType.vec2())
                .executes(c -> search(c.getSource(), isAppend,
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius"),
                        Vec2ArgumentType.getVec2(c, "center")))
                .build();
        ArgumentCommandNode<ServerCommandSource, DimensionType> argDimension = CommandManager.argument("dimension", DimensionArgumentType.dimension())
                .executes(c -> search(c.getSource(), isAppend,
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius"),
                        Vec2ArgumentType.getVec2(c, "center"),
                        DimensionArgumentType.getDimensionArgument(c, "dimension")))
                .build();

        actionNodeSearch.addChild(argSampleInterval);
        argSampleInterval.addChild(argSampleRadius);
        argSampleRadius.addChild(argCenter);
        argCenter.addChild(argDimension);

        return actionNodeSearch;
    }

    private static int printHelp(ServerCommandSource source)
    {
        CommandUtils.sendMessage(source, "Searches for the closest location of biomes around the center point.");
        CommandUtils.sendMessage(source, "Usage: /tellme locate-biome <search | search-append> <sample_interval> <sample_radius> [centerX centerZ] [dimension]");
        CommandUtils.sendMessage(source, "Usage: /tellme locate-biome output-data <to-chat | to-console | to-file> <ascii | csv>");
        CommandUtils.sendMessage(source, "search: Clears previously stored results, and then searches for the closest location of biomes");
        CommandUtils.sendMessage(source, "search-append: Searches for the closest location of biomes, appending the data to the previously stored results");
        CommandUtils.sendMessage(source, "  - The sample_interval is the distance between sample points, in blocks");
        CommandUtils.sendMessage(source, "  - The sample_radius is how many sample points are checked per side/axis");
        CommandUtils.sendMessage(source, "output-data: Outputs the stored data from previous searches to the selected output location. The 'file' output's dump files will go to 'config/tellme/'.");

        return 1;
    }

    private static int search(ServerCommandSource source, boolean append, int sampleInterval, int sampleRadius) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();
        Vec2f center = entity != null ? new Vec2f((float) entity.x, (float) entity.z) : Vec2f.ZERO;
        return search(source, append, sampleInterval, sampleRadius, center);
    }

    private static int search(ServerCommandSource source, boolean append, int sampleInterval, int sampleRadius, Vec2f center) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity == null)
        {
            throw CommandUtils.NO_DIMENSION_EXCEPTION.create();
        }

        return search(source, append, sampleInterval, sampleRadius, center, entity.dimension);
    }

    private static int search(ServerCommandSource source, boolean append, int sampleInterval, int sampleRadius, Vec2f center, DimensionType dimension) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();
        BiomeLocator biomeLocator = getBiomeLocatorFor(entity);
        World world = TellMe.dataProvider.getWorld(source.getMinecraftServer(), dimension);
        BiomeSource biomeProvider = world.getChunkManager().getChunkGenerator().getBiomeSource();

        CommandUtils.sendMessage(source, "Finding closest biome locations...");

        biomeLocator.setAppend(append);
        biomeLocator.findClosestBiomePositions(biomeProvider, new BlockPos(center.x, 0, center.y), sampleInterval, sampleRadius);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int outputData(ServerCommandSource source, OutputType outputType, DataDump.Format format)
    {
        Entity entity = source.getEntity();
        BiomeLocator biomeLocator = getBiomeLocatorFor(entity);
        List<String> lines = biomeLocator.getClosestBiomePositions(format);

        OutputUtils.printOutput(lines, outputType, format, "biome_locate", source);

        return 1;
    }

    private static BiomeLocator getBiomeLocatorFor(@Nullable Entity entity)
    {
        if (entity == null)
        {
            return CONSOLE_BIOME_LOCATOR;
        }

        return BIOME_LOCATORS.computeIfAbsent(entity.getUuid(), (e) -> new BiomeLocator());
    }
}
