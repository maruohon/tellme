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
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.util.BiomeLocator;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandBiomeLocate
{
    private static final Map<UUID, BiomeLocator> BIOME_LOCATORS = Maps.newHashMap();
    private static BiomeLocator consoleBiomeLocator;

    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("locate-biome").executes(c -> printHelp(c.getSource())).build();

        LiteralCommandNode<CommandSource> actionNodeOutputData = Commands.literal("output-data").build();
        ArgumentCommandNode<CommandSource, OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> outputData(c.getSource(),
                        c.getArgument("output_type", OutputType.class),
                        DataDump.Format.ASCII))
                .build();
        ArgumentCommandNode<CommandSource, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create())
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

    private static LiteralCommandNode<CommandSource> createNodes(String command, boolean isAppend)
    {
        LiteralCommandNode<CommandSource> actionNodeSearch = Commands.literal(command).build();

        ArgumentCommandNode<CommandSource, Integer> argSampleInterval = Commands.argument("sample_interval", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).build();
        ArgumentCommandNode<CommandSource, Integer> argSampleRadius = Commands.argument("sample_radius",   IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                .executes(c -> search(c.getSource(), isAppend,
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius")))
                .build();

        ArgumentCommandNode<CommandSource, ILocationArgument> argCenter = Commands.argument("center", Vec2Argument.vec2())
                .executes(c -> search(c.getSource(), isAppend,
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius"),
                        Vec2Argument.getVec2(c, "center")))
                .build();
        ArgumentCommandNode<CommandSource, ResourceLocation> argDimension = Commands.argument("dimension", DimensionArgument.dimension())
                .executes(c -> search(c.getSource(), isAppend,
                        IntegerArgumentType.getInteger(c, "sample_interval"),
                        IntegerArgumentType.getInteger(c, "sample_radius"),
                        Vec2Argument.getVec2(c, "center"),
                        DimensionArgument.getDimension(c, "dimension")))
                .build();

        actionNodeSearch.addChild(argSampleInterval);
        argSampleInterval.addChild(argSampleRadius);
        argSampleRadius.addChild(argCenter);
        argCenter.addChild(argDimension);

        return actionNodeSearch;
    }

    private static int printHelp(CommandSource source)
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

    private static int search(CommandSource source, boolean append, int sampleInterval, int sampleRadius) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();
        Vector2f center = entity != null ? new Vector2f((float) entity.getX(), (float) entity.getZ()) : Vector2f.ZERO;
        return search(source, append, sampleInterval, sampleRadius, center);
    }

    private static int search(CommandSource source, boolean append, int sampleInterval, int sampleRadius, Vector2f center) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity == null)
        {
            throw CommandUtils.NO_DIMENSION_EXCEPTION.create();
        }

        return search(source, append, sampleInterval, sampleRadius, center, entity.getCommandSenderWorld());
    }

    private static int search(CommandSource source, boolean append, int sampleInterval, int sampleRadius, Vector2f center, World world)
    {
        Entity entity = source.getEntity();
        BiomeLocator biomeLocator = getBiomeLocatorFor(source, entity);
        BiomeManager biomeManager = world.getBiomeManager();

        CommandUtils.sendMessage(source, "Finding closest biome locations...");

        biomeLocator.setAppend(append);
        biomeLocator.findClosestBiomePositions(biomeManager, new BlockPos(center.x, 0, center.y), sampleInterval, sampleRadius);

        CommandUtils.sendMessage(source, "Done");

        return 1;
    }

    private static int outputData(CommandSource source, OutputType outputType, DataDump.Format format)
    {
        Entity entity = source.getEntity();
        BiomeLocator biomeLocator = getBiomeLocatorFor(source, entity);
        List<String> lines = biomeLocator.getClosestBiomePositions(format);

        OutputUtils.printOutput(lines, outputType, format, "biome_locate", source);

        return 1;
    }

    private static BiomeLocator getBiomeLocatorFor(final CommandSource source, @Nullable final Entity entity)
    {
        if (entity == null)
        {
            if (consoleBiomeLocator == null)
            {
                consoleBiomeLocator = new BiomeLocator(source.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
            }

            return consoleBiomeLocator;
        }

        return BIOME_LOCATORS.computeIfAbsent(entity.getUUID(), (e) -> new BiomeLocator(source.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)));
    }
}
