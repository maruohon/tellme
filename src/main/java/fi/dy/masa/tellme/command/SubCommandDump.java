package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Nullable;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

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
import fi.dy.masa.tellme.datadump.MobEffectDump;
import fi.dy.masa.tellme.datadump.ModListDump;
import fi.dy.masa.tellme.datadump.PaintingTypesDump;
import fi.dy.masa.tellme.datadump.PoiTypesDump;
import fi.dy.masa.tellme.datadump.PotionDump;
import fi.dy.masa.tellme.datadump.SimpleForgeRegistryKeyOnlyDump;
import fi.dy.masa.tellme.datadump.SimpleVanillaRegistryKeyOnlyDump;
import fi.dy.masa.tellme.datadump.SoundEventDump;
import fi.dy.masa.tellme.datadump.SpawnEggDump;
import fi.dy.masa.tellme.datadump.StatTypesDump;
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

    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("dump").build();
        ArgumentCommandNode<CommandSourceStack, OutputType> outputTypeNode = Commands.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<CommandSourceStack, DataDump.Format> outputFormatNode = Commands.argument("output_format", OutputFormatArgument.create()).build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<CommandSourceStack, List<String>> dumpTypesNode =
                Commands.argument("dump_types",
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

    private static int execute(CommandContext<CommandSourceStack> ctx, OutputType outputType, DataDump.Format format, List<String> types) throws CommandSyntaxException
    {
        HashMap<String, DumpLineProvider> providers = getDumpProviders();
        CommandSourceStack source = ctx.getSource();
        @Nullable Entity entity = source.getEntity();
        @Nullable ServerLevel world = source.getLevel();
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
                ctx.getSource().sendFailure(Component.literal("No such dump type: '" + name + "'"));
            }
        }

        return 1;
    }

    public static void outputData(CommandContext<CommandSourceStack> ctx,
            DumpLineProvider provider, DumpContext context, String name, OutputType outputType, DataDump.Format format)
    {
        CommandSourceStack source = ctx.getSource();
        List<String> data = provider.getLines(context);

        if (data.isEmpty())
        {
            source.sendFailure(Component.literal("No data available for dump '" + name + "'"));
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
        dumpProviders.put("all-registry-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.REGISTRY));
        dumpProviders.put("advancements-simple",        (ctx) -> AdvancementDump.getFormattedAdvancementDumpSimple(ctx.format, ctx.server));
        dumpProviders.put("biome-sources",              (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.BIOME_SOURCE));
        dumpProviders.put("biomes-basic",               (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.BASIC));
        dumpProviders.put("biomes-with-colors",         (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.COLORS));
        dumpProviders.put("biomes-with-mob-spawns",     (ctx) -> BiomeDump.getFormattedBiomeDumpWithMobSpawns(ctx.format, ctx.world));
        dumpProviders.put("biomes-with-types",          (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.TYPES));
        dumpProviders.put("biomes-id-to-name",          (ctx) -> BiomeDump.getBiomeDumpIdToName(ctx.format, ctx.world));
        dumpProviders.put("block-entities",             (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.BLOCK_ENTITY_TYPES));
        dumpProviders.put("block-material-colors",      (ctx) -> BlockDump.getFormattedBlockToMapColorDump(ctx.format, ctx.world));
        dumpProviders.put("block-predicate-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.BLOCK_PREDICATE_TYPES));
        dumpProviders.put("block-props",                (ctx) -> BlockDump.getFormattedBlockPropertiesDump(ctx.format));
        dumpProviders.put("block-state-provider-types", (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.BLOCK_STATE_PROVIDER_TYPES));
        dumpProviders.put("blocks",                     (ctx) -> BlockDump.getFormattedBlockDump(ctx.format, false));
        dumpProviders.put("blocks-with-tags",           (ctx) -> BlockDump.getFormattedBlockDump(ctx.format, true));
        dumpProviders.put("blockstates-by-block",       (ctx) -> BlockStatesDump.getFormattedBlockStatesDumpByBlock());
        dumpProviders.put("blockstates-by-state",       (ctx) -> BlockStatesDump.getFormattedBlockStatesDumpByState(ctx.format));
        dumpProviders.put("chunk-generator-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.CHUNK_GENERATOR));
        dumpProviders.put("chunk-status",               (ctx) -> ChunkStatusDump.getFormattedDump(ctx.format));
        dumpProviders.put("commands",                   (ctx) -> CommandDump.getFormattedCommandDump(ctx.format, ctx.server));
        dumpProviders.put("creative-tabs",              (ctx) -> ItemGroupDump.getFormattedCreativetabDump(ctx.format));
        dumpProviders.put("custom-stats",               (ctx) -> StatTypesDump.getFormattedDumpCustomStats(ctx.format));
        dumpProviders.put("dimensions",                 (ctx) -> DimensionDump.getFormattedDimensionDump(ctx.format, ctx.server, false));
        dumpProviders.put("dimensions-verbose",         (ctx) -> DimensionDump.getFormattedDimensionDump(ctx.format, ctx.server, true));
        dumpProviders.put("enchantments",               (ctx) -> EnchantmentDump.getFormattedEnchantmentDump(ctx.format));
        dumpProviders.put("entities",                   (ctx) -> EntityDump.getFormattedEntityDump(null, ctx.format, false));
        dumpProviders.put("entities-with-class",        (ctx) -> EntityDump.getFormattedEntityDump(ctx.world, ctx.format, true));
        dumpProviders.put("entity-attributes",          (ctx) -> EntityDump.getFormattedEntityAttributeDump(ctx.format));
        dumpProviders.put("features",                   (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.FEATURES));
        dumpProviders.put("feature-size-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.FEATURE_SIZE_TYPES));
        dumpProviders.put("float-provider-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.FLOAT_PROVIDER_TYPES));
        dumpProviders.put("fluids",                     (ctx) -> FluidRegistryDump.getFormattedFluidRegistryDump(ctx.format));
        dumpProviders.put("foliage-placer-types",       (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.FOLIAGE_PLACER_TYPES));
        dumpProviders.put("food-items",                 (ctx) -> FoodItemDump.getFormattedFoodItemDump(ctx.format));
        dumpProviders.put("game-events",                (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.GAME_EVENT));
        dumpProviders.put("height-provider-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.HEIGHT_PROVIDER_TYPES));
        dumpProviders.put("int-provider-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.INT_PROVIDER_TYPES));
        dumpProviders.put("items",                      (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_BASIC));
        dumpProviders.put("items-craftable",            (ctx) -> ItemDump.getFormattedCraftableItemsDump(ctx.format, ctx.server));
        dumpProviders.put("items-damageable",           (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_DAMAGEABLES));
        dumpProviders.put("items-plantable",            (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_PLANTABLES));
        dumpProviders.put("items-registry-name-only",   (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.ITEMS));
        dumpProviders.put("items-tiered",               (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_TIERED));
        dumpProviders.put("items-with-tags",            (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_TAGS));
        dumpProviders.put("loot-condition-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.LOOT_CONDITION_TYPE));
        dumpProviders.put("loot-function-types",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.LOOT_FUNCTION_TYPE));
        dumpProviders.put("loot-nbt-provider-types",    (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.LOOT_NBT_PROVIDER_TYPE));
        dumpProviders.put("loot-number-provider-types", (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.LOOT_NUMBER_PROVIDER_TYPE));
        dumpProviders.put("loot-pool-entry-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.LOOT_POOL_ENTRY_TYPE));
        dumpProviders.put("loot-score-provider-types",  (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.LOOT_SCORE_PROVIDER_TYPE));
        dumpProviders.put("material-condition-types",   (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.CONDITION));
        dumpProviders.put("material-rule-types",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.RULE));
        dumpProviders.put("memory-module-types",        (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.MEMORY_MODULE_TYPES));
        dumpProviders.put("menu-types",                 (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.MENU_TYPES));
        dumpProviders.put("mob-effects",                (ctx) -> MobEffectDump.getFormattedPotionDump(ctx.format));
        dumpProviders.put("mod-list",                   (ctx) -> ModListDump.getFormattedModListDump(ctx.format));
        dumpProviders.put("painting-types",             (ctx) -> PaintingTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("particle-types",             (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.PARTICLE_TYPES));
        dumpProviders.put("placement-modifier-types",   (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.PLACEMENT_MODIFIERS));
        dumpProviders.put("players",                    (ctx) -> EntityInfo.getPlayerList(ctx.format, ctx.server));
        dumpProviders.put("player-nbt",                 (ctx) -> EntityInfo.getFullEntityInfo(ctx.entity, false));
        dumpProviders.put("poi-types",                  (ctx) -> PoiTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("position-rule-tests",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.POS_RULE_TEST));
        dumpProviders.put("position-source-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.POSITION_SOURCE_TYPE));
        dumpProviders.put("potions",                    (ctx) -> PotionDump.getFormattedPotionDump(ctx.format));
        dumpProviders.put("recipe-serializers",         (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.RECIPE_SERIALIZERS));
        dumpProviders.put("recipe-types",               (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.RECIPE_TYPES));
        dumpProviders.put("rule-test-types",            (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.RULE_TEST));
        dumpProviders.put("schedules",                  (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.SCHEDULES));
        dumpProviders.put("sensor-types",               (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.SENSOR_TYPES));
        dumpProviders.put("sound-events",               (ctx) -> SoundEventDump.getFormattedSoundEventDump(ctx.format));
        dumpProviders.put("spawn-eggs",                 (ctx) -> SpawnEggDump.getFormattedSpawnEggDump(ctx.format));
        dumpProviders.put("stat-types",                 (ctx) -> StatTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("structure-piece-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_PIECE));
        dumpProviders.put("structure-placement",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_PLACEMENT_TYPE));
        dumpProviders.put("structure-pool-element",     (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_POOL_ELEMENT));
        dumpProviders.put("structure-processor-types",  (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_PROCESSOR));
        dumpProviders.put("structure-type",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_TYPES));
        dumpProviders.put("tags-block",                 (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.BLOCK, false));
        dumpProviders.put("tags-block-split",           (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.BLOCK, true));
        dumpProviders.put("tags-entitytype",            (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ENTITY_TYPE, false));
        dumpProviders.put("tags-entitytype-split",      (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ENTITY_TYPE, true));
        dumpProviders.put("tags-fluid",                 (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.FLUID, false));
        dumpProviders.put("tags-fluid-split",           (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.FLUID, true));
        dumpProviders.put("tags-item",                  (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ITEM, false));
        dumpProviders.put("tags-item-split",            (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ITEM, true));
        dumpProviders.put("tree-decorator-types",       (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.TREE_DECORATOR_TYPES));
        dumpProviders.put("trunk-placer-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.TRUNK_PLACER_TYPES));
        dumpProviders.put("villager-professions",       (ctx) -> VillagerProfessionDump.getFormattedVillagerProfessionDump(ctx.format));
        dumpProviders.put("villager-trades",            (ctx) -> VillagerTradesDump.getFormattedVillagerTradesDump(ctx.format, ctx.entity));
        dumpProviders.put("villager-types",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.VILLAGER_TYPE));
        dumpProviders.put("world-carvers",              (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.WORLD_CARVERS));
        dumpProviders.put("world-presets",              (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, ctx.world.registryAccess().registryOrThrow(Registry.WORLD_PRESET_REGISTRY)));

        return DUMP_PROVIDERS;
    }

    public interface DumpLineProvider
    {
        List<String> getLines(DumpContext ctx);
    }

    public static class DumpContext
    {
        @Nullable public final Level world;
        @Nullable public final Entity entity;
        @Nullable public final MinecraftServer server;
        public final DataDump.Format format;

        public DumpContext(@Nullable Level world, @Nullable Entity entity, @Nullable MinecraftServer server, DataDump.Format format)
        {
            this.world = world;
            this.entity = entity;
            this.server = server;
            this.format = format;
        }
    }
}
