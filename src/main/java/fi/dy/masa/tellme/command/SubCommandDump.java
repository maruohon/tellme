package fi.dy.masa.tellme.command;

import java.util.Collections;
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
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
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
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DimensionDump;
import fi.dy.masa.tellme.datadump.EnchantmentDump;
import fi.dy.masa.tellme.datadump.EntityDump;
import fi.dy.masa.tellme.datadump.FeatureDump;
import fi.dy.masa.tellme.datadump.FluidRegistryDump;
import fi.dy.masa.tellme.datadump.FoodItemDump;
import fi.dy.masa.tellme.datadump.ItemDump;
import fi.dy.masa.tellme.datadump.ItemGroupDump;
import fi.dy.masa.tellme.datadump.ModListDump;
import fi.dy.masa.tellme.datadump.PaintingTypesDump;
import fi.dy.masa.tellme.datadump.PoiTypesDump;
import fi.dy.masa.tellme.datadump.PotionDump;
import fi.dy.masa.tellme.datadump.PotionTypeDump;
import fi.dy.masa.tellme.datadump.SimpleForgeRegistryKeyOnlyDump;
import fi.dy.masa.tellme.datadump.SimpleVanillaRegistryKeyOnlyDump;
import fi.dy.masa.tellme.datadump.SoundEventDump;
import fi.dy.masa.tellme.datadump.SpawnEggDump;
import fi.dy.masa.tellme.datadump.StatTypesDump;
import fi.dy.masa.tellme.datadump.TagDump;
import fi.dy.masa.tellme.datadump.TileEntityDump;
import fi.dy.masa.tellme.datadump.VillagerProfessionDump;
import fi.dy.masa.tellme.datadump.VillagerTradesDump;
import fi.dy.masa.tellme.datadump.WorldTypeDump;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.OutputUtils;
import net.minecraftforge.registries.ForgeRegistries;

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

        // Don't bother outputting anything else a second time, if outputting everything once anyway
        if (types.contains("all"))
        {
            for (Map.Entry<String, DumpLineProvider> entry : providers.entrySet())
            {
                String name = entry.getKey();
                DumpLineProvider provider = entry.getValue();

                try
                {
                    outputData(ctx, provider, name, outputType, format);
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
                        outputData(ctx, provider, name, outputType, format);
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
            DumpLineProvider provider, String name, OutputType outputType, DataDump.Format format) throws CommandSyntaxException
    {
        CommandSource source = ctx.getSource();
        @Nullable Entity entity = source.getEntity();
        @Nullable World world = entity.getEntityWorld();
        List<String> data = provider.getLines(world, entity, format);

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

        dumpProviders.put("activities",                 (world, entity, format) -> ActivityDump.getFormattedDump(format));
        dumpProviders.put("advancements-simple",        (world, entity, format) -> AdvancementDump.getFormattedAdvancementDumpSimple(format));
        dumpProviders.put("biome-provider-types",       (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.BIOME_PROVIDER_TYPES));
        dumpProviders.put("biomes",                     (world, entity, format) -> BiomeDump.getFormattedBiomeDump(format, false));
        dumpProviders.put("biomes-with-colors",         (world, entity, format) -> BiomeDump.getFormattedBiomeDump(format, true));
        dumpProviders.put("biomes-with-mob-spawns",     (world, entity, format) -> BiomeDump.getFormattedBiomeDumpWithMobSpawns(format));
        dumpProviders.put("biomes-id-to-name",          (world, entity, format) -> BiomeDump.getBiomeDumpIdToName(format));
        dumpProviders.put("block-props",                (world, entity, format) -> BlockDump.getFormattedBlockPropertiesDump(format));
        dumpProviders.put("blocks",                     (world, entity, format) -> BlockDump.getFormattedBlockDump(format, false));
        dumpProviders.put("blocks-with-nbt",            (world, entity, format) -> BlockDump.getFormattedBlockDump(format, true));
        dumpProviders.put("blockstates-by-block",       (world, entity, format) -> BlockStatesDump.getFormattedBlockStatesDumpByBlock());
        dumpProviders.put("blockstates-by-state",       (world, entity, format) -> BlockStatesDump.getFormattedBlockStatesDumpByState(format));
        dumpProviders.put("chunk-generator-types",      (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.CHUNK_GENERATOR_TYPES));
        dumpProviders.put("chunk-status",               (world, entity, format) -> ChunkStatusDump.getFormattedDump(format));
        dumpProviders.put("commands",                   (world, entity, format) -> CommandDump.getFormattedCommandDump(format));
        dumpProviders.put("container-types",            (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.CONTAINERS));
        dumpProviders.put("creative-tabs",              (world, entity, format) -> ItemGroupDump.getFormattedCreativetabDump(format));
        dumpProviders.put("custom-stats",               (world, entity, format) -> StatTypesDump.getFormattedDumpCustomStats(format));
        dumpProviders.put("decorators",                 (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.DECORATORS));
        dumpProviders.put("dimensions",                 (world, entity, format) -> DimensionDump.getFormattedDimensionDump(format));
        dumpProviders.put("enchantments",               (world, entity, format) -> EnchantmentDump.getFormattedEnchantmentDump(format));
        dumpProviders.put("entities",                   (world, entity, format) -> EntityDump.getFormattedEntityDump(null, format, false));
        dumpProviders.put("entities-with-class",        (world, entity, format) -> EntityDump.getFormattedEntityDump(world, format, true));
        dumpProviders.put("features",                   (world, entity, format) -> FeatureDump.getFormattedDump(format));
        dumpProviders.put("fluids",                     (world, entity, format) -> FluidRegistryDump.getFormattedFluidRegistryDump(format));
        dumpProviders.put("food-items",                 (world, entity, format) -> FoodItemDump.getFormattedFoodItemDump(format));
        dumpProviders.put("items",                      (world, entity, format) -> ItemDump.getFormattedItemDump(format, ItemDump.INFO_BASIC));
        dumpProviders.put("items-craftable",            (world, entity, format) -> ItemDump.getFormattedCraftableItemsDump(format));
        dumpProviders.put("items-plantable",            (world, entity, format) -> ItemDump.getFormattedItemDump(format, ItemDump.INFO_PLANTABLES));
        dumpProviders.put("items-with-nbt",             (world, entity, format) -> ItemDump.getFormattedItemDump(format, ItemDump.INFO_NBT));
        dumpProviders.put("items-with-tool-classes",    (world, entity, format) -> ItemDump.getFormattedItemDump(format, ItemDump.INFO_TOOL_CLASS));
        dumpProviders.put("memory-module-types",        (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.MEMORY_MODULE_TYPES));
        dumpProviders.put("mod-dimensions",             (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.MOD_DIMENSIONS));
        dumpProviders.put("mod-list",                   (world, entity, format) -> ModListDump.getFormattedModListDump(format));
        dumpProviders.put("music-types",                (world, entity, format) -> SoundEventDump.getFormattedMusicTypeDump(format));
        dumpProviders.put("painting-types",             (world, entity, format) -> PaintingTypesDump.getFormattedDump(format));
        dumpProviders.put("particle-types",             (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.PARTICLE_TYPES));
        dumpProviders.put("players",                    (world, entity, format) -> EntityInfo.getPlayerList(format));
        dumpProviders.put("player-nbt",                 (world, entity, format) -> entity != null ? EntityInfo.getFullEntityInfo(entity, false) : Collections.emptyList());
        dumpProviders.put("poi-types",                  (world, entity, format) -> PoiTypesDump.getFormattedDump(format));
        dumpProviders.put("potions",                    (world, entity, format) -> PotionDump.getFormattedPotionDump(format));
        dumpProviders.put("potion-types",               (world, entity, format) -> PotionTypeDump.getFormattedPotionTypeDump(format));
        dumpProviders.put("recipe-serializers",         (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.RECIPE_SERIALIZERS));
        dumpProviders.put("recipe-types",               (world, entity, format) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(format, Registry.RECIPE_TYPE));
        dumpProviders.put("rule-test-types",            (world, entity, format) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(format, Registry.RULE_TEST));
        dumpProviders.put("schedules",                  (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.SCHEDULES));
        dumpProviders.put("sensor-types",               (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.SENSOR_TYPES));
        dumpProviders.put("sound-events",               (world, entity, format) -> SoundEventDump.getFormattedSoundEventDump(format));
        dumpProviders.put("spawn-eggs",                 (world, entity, format) -> SpawnEggDump.getFormattedSpawnEggDump(format));
        dumpProviders.put("stat-types",                 (world, entity, format) -> StatTypesDump.getFormattedDump(format));
        dumpProviders.put("structure-pieces",           (world, entity, format) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(format, Registry.STRUCTURE_PIECE));
        dumpProviders.put("structure-pool-element",     (world, entity, format) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(format, Registry.STRUCTURE_POOL_ELEMENT));
        dumpProviders.put("structure-processor-types",  (world, entity, format) -> SimpleVanillaRegistryKeyOnlyDump.getFormattedDump(format, Registry.STRUCTURE_PROCESSOR));
        dumpProviders.put("surface-builders",           (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.SURFACE_BUILDERS));
        dumpProviders.put("tags-block",                 (world, entity, format) -> TagDump.getFormattedTagDump(format, TagDump.TagType.BLOCK, false));
        dumpProviders.put("tags-block-split",           (world, entity, format) -> TagDump.getFormattedTagDump(format, TagDump.TagType.BLOCK, true));
        dumpProviders.put("tags-entitytype",            (world, entity, format) -> TagDump.getFormattedTagDump(format, TagDump.TagType.ENTITY_TYPE, false));
        dumpProviders.put("tags-entitytype-split",      (world, entity, format) -> TagDump.getFormattedTagDump(format, TagDump.TagType.ENTITY_TYPE, true));
        dumpProviders.put("tags-fluid",                 (world, entity, format) -> TagDump.getFormattedTagDump(format, TagDump.TagType.FLUID, false));
        dumpProviders.put("tags-fluid-split",           (world, entity, format) -> TagDump.getFormattedTagDump(format, TagDump.TagType.FLUID, true));
        dumpProviders.put("tags-item",                  (world, entity, format) -> TagDump.getFormattedTagDump(format, TagDump.TagType.ITEM, false));
        dumpProviders.put("tags-item-split",            (world, entity, format) -> TagDump.getFormattedTagDump(format, TagDump.TagType.ITEM, true));
        dumpProviders.put("tile-entities",              (world, entity, format) -> TileEntityDump.getFormattedTileEntityDump(format));
        dumpProviders.put("villager-professions",       (world, entity, format) -> VillagerProfessionDump.getFormattedVillagerProfessionDump(format));
        dumpProviders.put("villager-trades",            (world, entity, format) -> VillagerTradesDump.getFormattedVillagerTradesDump(format));
        dumpProviders.put("world-carvers",              (world, entity, format) -> SimpleForgeRegistryKeyOnlyDump.getFormattedDump(format, ForgeRegistries.WORLD_CARVERS));
        dumpProviders.put("world-types",                (world, entity, format) -> WorldTypeDump.getFormattedWorldTypeDump(format));

        return DUMP_PROVIDERS;
    }

    public interface DumpLineProvider
    {
        List<String> getLines(@Nullable World world, @Nullable Entity entity, DataDump.Format format);
    }
}
