package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Nullable;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.datadump.ActivityDump;
import fi.dy.masa.tellme.datadump.AdvancementDump;
import fi.dy.masa.tellme.datadump.BiomeDump;
import fi.dy.masa.tellme.datadump.BlockDump;
import fi.dy.masa.tellme.datadump.BlockStatesDump;
import fi.dy.masa.tellme.datadump.ChunkStatusDump;
import fi.dy.masa.tellme.datadump.CommandDump;
import fi.dy.masa.tellme.datadump.DimensionDump;
import fi.dy.masa.tellme.datadump.EnchantmentDump;
import fi.dy.masa.tellme.datadump.EntityDump;
import fi.dy.masa.tellme.datadump.FluidRegistryDump;
import fi.dy.masa.tellme.datadump.FoodItemDump;
import fi.dy.masa.tellme.datadump.ItemDump;
import fi.dy.masa.tellme.datadump.ItemGroupDump;
import fi.dy.masa.tellme.datadump.ModListDump;
import fi.dy.masa.tellme.datadump.PaintingTypesDump;
import fi.dy.masa.tellme.datadump.PoiTypesDump;
import fi.dy.masa.tellme.datadump.PotionDump;
import fi.dy.masa.tellme.datadump.SimpleVanillaRegistryKeyOnlyDump;
import fi.dy.masa.tellme.datadump.SoundEventDump;
import fi.dy.masa.tellme.datadump.SpawnEggDump;
import fi.dy.masa.tellme.datadump.StatTypesDump;
import fi.dy.masa.tellme.datadump.StatusEffectDump;
import fi.dy.masa.tellme.datadump.TagDump;
import fi.dy.masa.tellme.datadump.VillagerProfessionDump;
import fi.dy.masa.tellme.datadump.VillagerTradesDump;
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
        @Nullable MinecraftServer server = source.getServer();
        DumpContext dumpContext = new DumpContext(world, entity, server, format);

        if (types.contains("all"))
        {
            types = new ArrayList<>(providers.keySet());
        }

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
                ctx.getSource().sendError(Text.literal("No such dump type: '" + name + "'"));
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
            source.sendError(Text.literal("No data available for dump '" + name + "'"));
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
        dumpProviders.put("all-registry-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.REGISTRIES));
        dumpProviders.put("advancements-simple",        (ctx) -> AdvancementDump.getFormattedAdvancementDumpSimple(ctx.format, ctx.server));
        dumpProviders.put("biome-sources",              (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.BIOME_SOURCE));
        dumpProviders.put("biomes-basic",               (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.BASIC));
        dumpProviders.put("biomes-with-colors",         (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.COLORS));
        dumpProviders.put("biomes-with-mob-spawns",     (ctx) -> BiomeDump.getFormattedBiomeDumpWithMobSpawns(ctx.format, ctx.world));
        dumpProviders.put("biomes-with-tags",           (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.TAGS));
        dumpProviders.put("biomes-id-to-name",          (ctx) -> BiomeDump.getBiomeDumpIdToName(ctx.format, ctx.world));
        dumpProviders.put("block-entities",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.BLOCK_ENTITY_TYPE));
        dumpProviders.put("block-material-colors",      (ctx) -> BlockDump.getFormattedBlockToMapColorDump(ctx.format, ctx.world));
        dumpProviders.put("block-predicate-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.BLOCK_PREDICATE_TYPE));
        dumpProviders.put("block-props",                (ctx) -> BlockDump.getFormattedBlockPropertiesDump(ctx.format));
        dumpProviders.put("block-state-provider-types", (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.BLOCK_STATE_PROVIDER_TYPE));
        dumpProviders.put("blocks",                     (ctx) -> BlockDump.getFormattedBlockDump(ctx.format, false));
        dumpProviders.put("blocks-with-tags",           (ctx) -> BlockDump.getFormattedBlockDump(ctx.format, true));
        dumpProviders.put("blockstates-by-block",       (ctx) -> BlockStatesDump.getFormattedBlockStatesDumpByBlock());
        dumpProviders.put("blockstates-by-state",       (ctx) -> BlockStatesDump.getFormattedBlockStatesDumpByState(ctx.format));
        dumpProviders.put("carvers",                    (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.CARVER));
        dumpProviders.put("chunk-generators",           (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.CHUNK_GENERATOR));
        dumpProviders.put("chunk-status",               (ctx) -> ChunkStatusDump.getFormattedDump(ctx.format));
        dumpProviders.put("commands",                   (ctx) -> CommandDump.getFormattedCommandDump(ctx.format, ctx.server));
        dumpProviders.put("container-types",            (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.SCREEN_HANDLER));
        dumpProviders.put("creative-tabs",              (ctx) -> ItemGroupDump.getFormattedCreativetabDump(ctx.format));
        dumpProviders.put("custom-stats",               (ctx) -> StatTypesDump.getFormattedDumpCustomStats(ctx.format));
        dumpProviders.put("dimensions",                 (ctx) -> DimensionDump.getFormattedDimensionDump(ctx.format, ctx.server, false));
        dumpProviders.put("dimensions-verbose",         (ctx) -> DimensionDump.getFormattedDimensionDump(ctx.format, ctx.server, true));
        dumpProviders.put("enchantments",               (ctx) -> EnchantmentDump.getFormattedEnchantmentDump(ctx.format));
        dumpProviders.put("entities",                   (ctx) -> EntityDump.getFormattedEntityDump(null, ctx.format, false));
        dumpProviders.put("entities-with-class",        (ctx) -> EntityDump.getFormattedEntityDump(ctx.world, ctx.format, true));
        dumpProviders.put("entity-attributes",          (ctx) -> EntityDump.getFormattedEntityAttributeDump(ctx.format));
        dumpProviders.put("features",                   (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.FEATURE));
        dumpProviders.put("feature-size-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.FEATURE_SIZE_TYPE));
        dumpProviders.put("fluids",                     (ctx) -> FluidRegistryDump.getFormattedFluidRegistryDump(ctx.format));
        dumpProviders.put("float-provider-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.FLOAT_PROVIDER_TYPE));
        dumpProviders.put("foliage-placer-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.FOLIAGE_PLACER_TYPE));
        dumpProviders.put("food-items",                 (ctx) -> FoodItemDump.getFormattedFoodItemDump(ctx.format));
        dumpProviders.put("game-events",                (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.GAME_EVENT));
        dumpProviders.put("height-provider-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.HEIGHT_PROVIDER_TYPE));
        dumpProviders.put("int-provider-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.INT_PROVIDER_TYPE));
        dumpProviders.put("inventory-screens",          (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.SCREEN_HANDLER));
        dumpProviders.put("items",                      (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_BASIC));
        dumpProviders.put("items-craftable",            (ctx) -> ItemDump.getFormattedCraftableItemsDump(ctx.format, ctx.server));
        dumpProviders.put("items-damageable",           (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_DAMAGEABLES));
        dumpProviders.put("items-plantable",            (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_PLANTABLES));
        dumpProviders.put("items-registry-name",        (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_REG_NAME));
        dumpProviders.put("items-with-tags",            (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_TAGS));
        dumpProviders.put("loot-condition-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.LOOT_CONDITION_TYPE));
        dumpProviders.put("loot-function-types",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.LOOT_FUNCTION_TYPE));
        dumpProviders.put("loot-nbt-provider-types",    (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.LOOT_NBT_PROVIDER_TYPE));
        dumpProviders.put("loot-number-provider-types", (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.LOOT_NUMBER_PROVIDER_TYPE));
        dumpProviders.put("loot-pool-entry-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.LOOT_POOL_ENTRY_TYPE));
        dumpProviders.put("loot-score-provider-types",  (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.LOOT_SCORE_PROVIDER_TYPE));
        dumpProviders.put("material-condition-types",   (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.MATERIAL_CONDITION));
        dumpProviders.put("material-rule-types",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.MATERIAL_RULE));
        dumpProviders.put("memory-module-types",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.MEMORY_MODULE_TYPE));
        dumpProviders.put("mod-list",                   (ctx) -> ModListDump.getFormattedModListDump(ctx.format));
        dumpProviders.put("painting-types",             (ctx) -> PaintingTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("particle-types",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.PARTICLE_TYPE));
        dumpProviders.put("placement-modifier-types",   (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.PLACEMENT_MODIFIER_TYPE));
        dumpProviders.put("players",                    (ctx) -> EntityInfo.getPlayerList(ctx.format, ctx.server));
        dumpProviders.put("player-nbt",                 (ctx) -> EntityInfo.getFullEntityInfo(ctx.entity, false));
        dumpProviders.put("poi-types",                  (ctx) -> PoiTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("position-rule-tests",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.POS_RULE_TEST));
        dumpProviders.put("position-source-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.POSITION_SOURCE_TYPE));
        dumpProviders.put("potions",                    (ctx) -> PotionDump.getFormattedPotionTypeDump(ctx.format));
        dumpProviders.put("recipe-serializers",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.RECIPE_SERIALIZER));
        dumpProviders.put("recipe-types",               (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.RECIPE_TYPE));
        dumpProviders.put("rule-tests",                 (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.RULE_TEST));
        dumpProviders.put("schedules",                  (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.SCHEDULE));
        dumpProviders.put("sensor-types",               (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.SENSOR_TYPE));
        dumpProviders.put("sound-events",               (ctx) -> SoundEventDump.getFormattedSoundEventDump(ctx.format));
        dumpProviders.put("spawn-eggs",                 (ctx) -> SpawnEggDump.getFormattedSpawnEggDump(ctx.format));
        dumpProviders.put("stat-types",                 (ctx) -> StatTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("status-effects",             (ctx) -> StatusEffectDump.getFormattedPotionDump(ctx.format));
        dumpProviders.put("structure-pieces",           (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.STRUCTURE_PIECE));
        dumpProviders.put("structure-placement",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.STRUCTURE_PLACEMENT));
        dumpProviders.put("structure-pool-elements",    (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.STRUCTURE_POOL_ELEMENT));
        dumpProviders.put("structure-processors",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.STRUCTURE_PROCESSOR));
        dumpProviders.put("structure-type",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.STRUCTURE_TYPE));
        dumpProviders.put("tags-block",                 (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.BLOCK, false));
        dumpProviders.put("tags-block-split",           (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.BLOCK, true));
        dumpProviders.put("tags-entitytype",            (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ENTITY_TYPE, false));
        dumpProviders.put("tags-entitytype-split",      (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ENTITY_TYPE, true));
        dumpProviders.put("tags-fluid",                 (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.FLUID, false));
        dumpProviders.put("tags-fluid-split",           (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.FLUID, true));
        dumpProviders.put("tags-item",                  (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ITEM, false));
        dumpProviders.put("tags-item-split",            (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ITEM, true));
        dumpProviders.put("tree-decorator-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.TREE_DECORATOR_TYPE));
        dumpProviders.put("trunk-placer-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.TRUNK_PLACER_TYPE));
        dumpProviders.put("villager-professions",       (ctx) -> VillagerProfessionDump.getFormattedVillagerProfessionDump(ctx.format));
        dumpProviders.put("villager-trades",            (ctx) -> VillagerTradesDump.getFormattedVillagerTradesDump(ctx.format, ctx.entity));
        dumpProviders.put("villager-types",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.VILLAGER_TYPE));
        dumpProviders.put("world-carvers",              (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registries.CARVER));
        dumpProviders.put("world-presets",              (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, ctx.world.getRegistryManager().get(RegistryKeys.WORLD_PRESET)));

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
