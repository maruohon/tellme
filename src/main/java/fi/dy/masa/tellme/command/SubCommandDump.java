package fi.dy.masa.tellme.command;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
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

    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("dump").build();
        ArgumentCommandNode<CommandSource, OutputType> outputTypeNode = Commands.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<CommandSource, DataDump.Format> outputFormatNode = Commands.argument("output_format", OutputFormatArgument.create()).build();

        @SuppressWarnings("unchecked")
        ArgumentCommandNode<CommandSource, List<String>> dumpTypesNode =
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

    private static int execute(CommandContext<CommandSource> ctx, OutputType outputType, DataDump.Format format, List<String> types) throws CommandSyntaxException
    {
        HashMap<String, DumpLineProvider> providers = getDumpProviders();
        CommandSource source = ctx.getSource();
        @Nullable Entity entity = source.getEntity();
        @Nullable ServerWorld world = source.getWorld();
        @Nullable MinecraftServer server = source.getServer();
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
                    ctx.getSource().sendErrorMessage(new StringTextComponent("No such dump type: '" + name + "'"));
                }
            }
        }

        return 1;
    }

    public static void outputData(CommandContext<CommandSource> ctx,
            DumpLineProvider provider, DumpContext context, String name, OutputType outputType, DataDump.Format format)
    {
        CommandSource source = ctx.getSource();
        List<String> data = provider.getLines(context);

        if (data.isEmpty())
        {
            source.sendErrorMessage(new StringTextComponent("No data available for dump '" + name + "'"));
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
        dumpProviders.put("biome-provider-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.field_239689_aA_));
        dumpProviders.put("biomes-basic",               (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.BASIC));
        dumpProviders.put("biomes-with-colors",         (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.COLORS));
        dumpProviders.put("biomes-with-feature-valid",  (ctx) -> BiomeDump.getFormattedBiomeDump(ctx.format, ctx.world, BiomeDump.VALIDITY));
        dumpProviders.put("biomes-with-mob-spawns",     (ctx) -> BiomeDump.getFormattedBiomeDumpWithMobSpawns(ctx.format));
        dumpProviders.put("biomes-id-to-name",          (ctx) -> BiomeDump.getBiomeDumpIdToName(ctx.format));
        dumpProviders.put("block-props",                (ctx) -> BlockDump.getFormattedBlockPropertiesDump(ctx.format));
        dumpProviders.put("block-placer-types",         (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.BLOCK_PLACER_TYPES));
        dumpProviders.put("block-state-provider-types", (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.BLOCK_STATE_PROVIDER_TYPES));
        dumpProviders.put("blocks",                     (ctx) -> BlockDump.getFormattedBlockDump(ctx.format, false));
        dumpProviders.put("blocks-with-tags",           (ctx) -> BlockDump.getFormattedBlockDump(ctx.format, true));
        dumpProviders.put("blockstates-by-block",       (ctx) -> BlockStatesDump.getFormattedBlockStatesDumpByBlock());
        dumpProviders.put("blockstates-by-state",       (ctx) -> BlockStatesDump.getFormattedBlockStatesDumpByState(ctx.format));
        dumpProviders.put("chunk-generator-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.field_239690_aB_));
        dumpProviders.put("chunk-status",               (ctx) -> ChunkStatusDump.getFormattedDump(ctx.format));
        dumpProviders.put("commands",                   (ctx) -> CommandDump.getFormattedCommandDump(ctx.format, ctx.server));
        dumpProviders.put("container-types",            (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.CONTAINERS));
        dumpProviders.put("creative-tabs",              (ctx) -> ItemGroupDump.getFormattedCreativetabDump(ctx.format));
        dumpProviders.put("custom-stats",               (ctx) -> StatTypesDump.getFormattedDumpCustomStats(ctx.format));
        dumpProviders.put("decorators",                 (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.DECORATORS));
        dumpProviders.put("dimensions",                 (ctx) -> DimensionDump.getFormattedDimensionDump(ctx.format, ctx.server, false));
        dumpProviders.put("dimensions-verbose",         (ctx) -> DimensionDump.getFormattedDimensionDump(ctx.format, ctx.server, true));
        dumpProviders.put("enchantments",               (ctx) -> EnchantmentDump.getFormattedEnchantmentDump(ctx.format));
        dumpProviders.put("entities",                   (ctx) -> EntityDump.getFormattedEntityDump(null, ctx.format, false));
        dumpProviders.put("entities-with-class",        (ctx) -> EntityDump.getFormattedEntityDump(ctx.world, ctx.format, true));
        dumpProviders.put("entity-attributes",          (ctx) -> EntityDump.getFormattedEntityAttributeDump(ctx.format));
        dumpProviders.put("features",                   (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.FEATURES));
        dumpProviders.put("feature-size-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.field_239702_ay_));
        dumpProviders.put("fluids",                     (ctx) -> FluidRegistryDump.getFormattedFluidRegistryDump(ctx.format));
        dumpProviders.put("foliage-placer-types",       (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.FOLIAGE_PLACER_TYPES));
        dumpProviders.put("food-items",                 (ctx) -> FoodItemDump.getFormattedFoodItemDump(ctx.format));
        dumpProviders.put("items",                      (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_BASIC));
        dumpProviders.put("items-craftable",            (ctx) -> ItemDump.getFormattedCraftableItemsDump(ctx.format, ctx.server));
        dumpProviders.put("items-plantable",            (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_PLANTABLES));
        dumpProviders.put("items-with-tags",            (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_TAGS));
        dumpProviders.put("items-with-tool-classes",    (ctx) -> ItemDump.getFormattedItemDump(ctx.format, ItemDump.INFO_TOOL_CLASS));
        dumpProviders.put("loot-condition-types",       (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.field_239704_ba_));
        dumpProviders.put("loot-function-types",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.field_239694_aZ_));
        dumpProviders.put("loot-pool-entry-types",      (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.field_239693_aY_));
        dumpProviders.put("memory-module-types",        (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.MEMORY_MODULE_TYPES));
        dumpProviders.put("mod-list",                   (ctx) -> ModListDump.getFormattedModListDump(ctx.format));
        dumpProviders.put("painting-types",             (ctx) -> PaintingTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("particle-types",             (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.PARTICLE_TYPES));
        dumpProviders.put("players",                    (ctx) -> EntityInfo.getPlayerList(ctx.format, ctx.server));
        dumpProviders.put("player-nbt",                 (ctx) -> EntityInfo.getFullEntityInfo(ctx.entity, false));
        dumpProviders.put("poi-types",                  (ctx) -> PoiTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("position-rule-tests",        (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.field_239691_aJ_));
        dumpProviders.put("potions",                    (ctx) -> PotionDump.getFormattedPotionDump(ctx.format));
        dumpProviders.put("potion-types",               (ctx) -> PotionTypeDump.getFormattedPotionTypeDump(ctx.format));
        dumpProviders.put("recipe-serializers",         (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.RECIPE_SERIALIZERS));
        dumpProviders.put("recipe-types",               (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.RECIPE_TYPE));
        dumpProviders.put("rule-test-types",            (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.RULE_TEST));
        dumpProviders.put("schedules",                  (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.SCHEDULES));
        dumpProviders.put("sensor-types",               (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.SENSOR_TYPES));
        dumpProviders.put("sound-events",               (ctx) -> SoundEventDump.getFormattedSoundEventDump(ctx.format));
        dumpProviders.put("spawn-eggs",                 (ctx) -> SpawnEggDump.getFormattedSpawnEggDump(ctx.format));
        dumpProviders.put("stat-types",                 (ctx) -> StatTypesDump.getFormattedDump(ctx.format));
        dumpProviders.put("structure-features",         (ctx) -> StructureFeatureDump.getFormattedDump(ctx.format, false));
        dumpProviders.put("structure-features-with-spawns", (ctx) -> StructureFeatureDump.getFormattedDump(ctx.format, true));
        dumpProviders.put("structure-pieces",           (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_PIECE));
        dumpProviders.put("structure-pool-element",     (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_POOL_ELEMENT));
        dumpProviders.put("structure-processor-types",  (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.STRUCTURE_PROCESSOR));
        dumpProviders.put("surface-builders",           (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.SURFACE_BUILDERS));
        dumpProviders.put("tags-block",                 (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.BLOCK, false));
        dumpProviders.put("tags-block-split",           (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.BLOCK, true));
        dumpProviders.put("tags-entitytype",            (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ENTITY_TYPE, false));
        dumpProviders.put("tags-entitytype-split",      (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ENTITY_TYPE, true));
        dumpProviders.put("tags-fluid",                 (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.FLUID, false));
        dumpProviders.put("tags-fluid-split",           (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.FLUID, true));
        dumpProviders.put("tags-item",                  (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ITEM, false));
        dumpProviders.put("tags-item-split",            (ctx) -> TagDump.getFormattedTagDump(ctx.format, TagDump.TagType.ITEM, true));
        dumpProviders.put("tile-entities",              (ctx) -> TileEntityDump.getFormattedTileEntityDump(ctx.format));
        dumpProviders.put("tree-decorator-types",       (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.TREE_DECORATOR_TYPES));
        dumpProviders.put("trunk-placer-types",         (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.field_239701_aw_));
        dumpProviders.put("villager-professions",       (ctx) -> VillagerProfessionDump.getFormattedVillagerProfessionDump(ctx.format));
        dumpProviders.put("villager-trades",            (ctx) -> VillagerTradesDump.getFormattedVillagerTradesDump(ctx.format, ctx.entity));
        dumpProviders.put("villager-types",             (ctx) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(ctx.format, Registry.VILLAGER_TYPE));
        dumpProviders.put("world-carvers",              (ctx) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(ctx.format, ForgeRegistries.WORLD_CARVERS));

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
