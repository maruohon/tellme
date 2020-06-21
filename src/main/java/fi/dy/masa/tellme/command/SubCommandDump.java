package fi.dy.masa.tellme.command;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.datadump.*;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandDump
{
    private static final HashMap<String, DumpLineProvider> DUMP_PROVIDERS = new LinkedHashMap<>();

    // /tellme dump <to-chat | to-console | to-file> <ascii | csv> <type> [type] ...

    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("dump").build();
        ArgumentCommandNode<ServerCommandSource, OutputType> outputTypeNode = CommandManager.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<ServerCommandSource, DataDump.Format> outputFormatNode = CommandManager.argument("output_format", OutputFormatArgument.create()).build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<ServerCommandSource, List<String>> dumpTypesNode =
                CommandManager.argument("dump_types",
                        StringCollectionArgument.create(() -> SubCommandDump.getDumpProviders().keySet(), "No dump types given"))
                .executes(c -> execute(c,
                        c.getArgument("output_type", OutputType.class),
                        c.getArgument("output_format", DataDump.Format.class),
                        (List<String>) c.getArgument("dump_types", List.class)))
                .build();

        subCommandRootNode.addChild(outputTypeNode);
        outputTypeNode.addChild(outputFormatNode);
        outputFormatNode.addChild(dumpTypesNode);

        return subCommandRootNode;
    }

    private static int execute(CommandContext<ServerCommandSource> ctx, OutputType outputType, DataDump.Format format, List<String> types)
    {
        HashMap<String, DumpLineProvider> providers = getDumpProviders();
        ServerCommandSource source = ctx.getSource();
        @Nullable Entity entity = source.getEntity();
        @Nullable ServerWorld world = source.getWorld();
        @Nullable MinecraftServer server = source.getMinecraftServer();
        DumpContext dumpContext = new DumpContext(world, entity, server, format);

        // Don't bother outputting anything else a second time, if outputting everything once anyway
        if (types.contains("all"))
        {
            for (Map.Entry<String, DumpLineProvider> entry : providers.entrySet())
            {
                String name = entry.getKey();
                DumpLineProvider provider = entry.getValue();

                try
                {
                    outputData(ctx, provider, dumpContext, name, outputType, format);
                }
                catch (Exception e)
                {
                    TellMe.logger.warn("Exception while dumping '{}'", name, e);
                }
            }
        }
        else
        {
            for (String name : types)
            {
                DumpLineProvider provider = providers.get(name);

                if (provider != null)
                {
                    try
                    {
                        outputData(ctx, provider, dumpContext, name, outputType, format);
                    }
                    catch (Exception e)
                    {
                        TellMe.logger.warn("Exception while dumping '{}'", name, e);
                    }
                }
                else
                {
                    ctx.getSource().sendError(new LiteralText("No such dump type: '" + name + "'"));
                }
            }
        }

        return 1;
    }

    public static void outputData(CommandContext<ServerCommandSource> ctx,
            DumpLineProvider provider, DumpContext context, String name, OutputType outputType, DataDump.Format format)
    {
        ServerCommandSource source = ctx.getSource();
        List<String> data = provider.getLines(context);

        if (data.isEmpty())
        {
            source.sendError(new LiteralText("No data available for dump '" + name + "'"));
            return;
        }

        OutputUtils.printOutput(data, outputType, format, name, source);
    }

    public static HashMap<String, DumpLineProvider> getDumpProviders()
    {
        if (DUMP_PROVIDERS.isEmpty() == false)
        {
            return DUMP_PROVIDERS;
        }

        HashMap<String, DumpLineProvider> dumpProviders = DUMP_PROVIDERS;

        dumpProviders.put("activities",                 (ctx) -> ActivityDump.getFormattedDump(ctx.format));
        dumpProviders.put("advancements-simple",        (ctx) -> AdvancementDump.getFormattedAdvancementDumpSimple(ctx.format, ctx.server));
        dumpProviders.put("biome-provider-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.BIOME_SOURCE_TYPE));
        dumpProviders.put("biomes-basic",               (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.BASIC));
        dumpProviders.put("biomes-with-colors",         (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.COLORS));
        dumpProviders.put("biomes-with-feature-valid",  (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.VALIDITY));
        dumpProviders.put("biomes-with-mob-spawns",     (ctx) -> BiomeDump.getFormattedBiomeDumpWithMobSpawns(ctx.format));
        dumpProviders.put("biomes-id-to-name",          (ctx) -> BiomeDump.getBiomeDumpIdToName(ctx.format));
        dumpProviders.put("block-props",                (ctx) -> BlockDump.getFormattedBlockPropertiesDump(ctx.format));
        dumpProviders.put("blocks",                     (ctx) -> BlockDump.getFormattedBlockDump(ctx.format, false));
        dumpProviders.put("blocks-with-tags",           (ctx) -> BlockDump.getFormattedBlockDump(ctx.format, true));
        dumpProviders.put("blockstates-by-block",       (ctx) -> BlockStatesDump.getFormattedBlockStatesDumpByBlock());
        dumpProviders.put("blockstates-by-state",       (ctx) -> BlockStatesDump.getFormattedBlockStatesDumpByState(ctx.format));
        dumpProviders.put("chunk-generator-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.CHUNK_GENERATOR_TYPE));
        dumpProviders.put("chunk-status",               (ctx) -> ChunkStatusDump.getFormattedDump(ctx.format));
        dumpProviders.put("commands",                   (ctx) -> CommandDump.getFormattedCommandDump(ctx.format, ctx.server));
        dumpProviders.put("container-types",            (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.CONTAINER));
        dumpProviders.put("creative-tabs",              (ctx) -> ItemGroupDump.getFormattedCreativetabDump(ctx.format));
        dumpProviders.put("custom-stats",               (ctx) -> StatTypesDump.getFormattedDumpCustomStats(ctx.format));
        dumpProviders.put("decorators",                 (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.DECORATOR));
        dumpProviders.put("dimensions",                 (ctx) -> DimensionDump.getFormattedDimensionDump(ctx.format, ctx.server));
        dumpProviders.put("enchantments",               (ctx) -> EnchantmentDump.getFormattedEnchantmentDump(ctx.format));
        dumpProviders.put("entities",                   (ctx) -> EntityDump.getFormattedEntityDump(null, ctx.format, false));
        dumpProviders.put("entities-with-class",        (ctx) -> EntityDump.getFormattedEntityDump(ctx.world, ctx.format, true));
        dumpProviders.put("features",                   (ctx) -> FeatureDump.getFormattedDump(ctx.format));
        dumpProviders.put("fluids",                     (ctx) -> FluidRegistryDump.getFormattedFluidRegistryDump(ctx.format));
        dumpProviders.put("food-items",                 (ctx) -> FoodItemDump.getFormattedFoodItemDump(ctx.format));
        dumpProviders.put("items",                      (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_BASIC));
        dumpProviders.put("items-craftable",            (ctx) -> ItemDump.getFormattedCraftableItemsDump(ctx.format, ctx.server));
        dumpProviders.put("items-plantable",            (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_PLANTABLES));
        dumpProviders.put("items-with-tags",            (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_TAGS));
        dumpProviders.put("level-generator-types",      (ctx) -> LevelGeneratorTypeDump.getFormattedWorldTypeDump(ctx.format));
        dumpProviders.put("memory-module-types",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.MEMORY_MODULE_TYPE));
        dumpProviders.put("mod-list",                   (ctx) -> ModListDump.getFormattedModListDump(ctx.format));
        dumpProviders.put("music-types",                (ctx) -> SoundEventDump.getFormattedMusicTypeDump(ctx.format));
        dumpProviders.put("painting-types",             (ctx) -> PaintingTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("particle-types",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.PARTICLE_TYPE));
        dumpProviders.put("players",                    (ctx) -> EntityInfo.getPlayerList(ctx.format, ctx.server));
        dumpProviders.put("player-nbt",                 (ctx) -> EntityInfo.getFullEntityInfo(ctx.entity, false));
        dumpProviders.put("poi-types",                  (ctx) -> PoiTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("potions",                    (ctx) -> PotionDump.getFormattedPotionTypeDump(ctx.format));
        dumpProviders.put("recipe-serializers",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.RECIPE_SERIALIZER));
        dumpProviders.put("recipe-types",               (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.RECIPE_TYPE));
        dumpProviders.put("rule-test-types",            (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.RULE_TEST));
        dumpProviders.put("schedules",                  (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.SCHEDULE));
        dumpProviders.put("sensor-types",               (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.SENSOR_TYPE));
        dumpProviders.put("sound-events",               (ctx) -> SoundEventDump.getFormattedSoundEventDump(ctx.format));
        dumpProviders.put("spawn-eggs",                 (ctx) -> SpawnEggDump.getFormattedSpawnEggDump(ctx.format));
        dumpProviders.put("stat-types",                 (ctx) -> StatTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("status-effects",             (ctx) -> StatusEffectDump.getFormattedPotionDump(ctx.format));
        dumpProviders.put("structure-pieces",           (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_PIECE));
        dumpProviders.put("structure-pool-element",     (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_POOL_ELEMENT));
        dumpProviders.put("structure-processor-types",  (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_PROCESSOR));
        dumpProviders.put("surface-builders",           (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.SURFACE_BUILDER));
        dumpProviders.put("tags-block",                 (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.BLOCK, false));
        dumpProviders.put("tags-block-split",           (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.BLOCK, true));
        dumpProviders.put("tags-entitytype",            (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ENTITY_TYPE, false));
        dumpProviders.put("tags-entitytype-split",      (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ENTITY_TYPE, true));
        dumpProviders.put("tags-fluid",                 (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.FLUID, false));
        dumpProviders.put("tags-fluid-split",           (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.FLUID, true));
        dumpProviders.put("tags-item",                  (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ITEM, false));
        dumpProviders.put("tags-item-split",            (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ITEM, true));
        dumpProviders.put("tile-entities",              (ctx) -> TileEntityDump.getFormattedTileEntityDump(ctx.format));
        dumpProviders.put("villager-professions",       (ctx) -> VillagerProfessionDump.getFormattedVillagerProfessionDump(ctx.format));
        dumpProviders.put("villager-trades",            (ctx) -> VillagerTradesDump.getFormattedVillagerTradesDump(ctx.format, ctx.entity));
        dumpProviders.put("villager-types",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.VILLAGER_TYPE));
        dumpProviders.put("world-carvers",              (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.CARVER));

        return DUMP_PROVIDERS;
    }

    public interface DumpLineProvider
    {
        List<String> getLines(DumpContext ctx);
    }

    public static class DumpContext
    {
        @Nullable public final World world;
        @Nullable public final Entity entity;
        @Nullable public final MinecraftServer server;
        public final DataDump.Format format;

        public DumpContext(@Nullable World world, @Nullable Entity entity, @Nullable MinecraftServer server, DataDump.Format format)
        {
            this.world = world;
            this.entity = entity;
            this.server = server;
            this.format = format;
        }
    }
}
