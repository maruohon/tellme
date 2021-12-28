package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.nbt.NbtStringifierPretty;

public class BlockInfo
{
    private static final Map<Material, String> MATERIAL_NAMES = getMaterialNames();

    private static Map<Material, String> getMaterialNames()
    {
        Map<Material, String> names = new HashMap<>();

        names.put(Material.AIR, "AIR");
        names.put(Material.HEAVY_METAL, "ANVIL");
        names.put(Material.BAMBOO, "BAMBOO");
        names.put(Material.BAMBOO_SAPLING, "BAMBOO_SAPLING");
        names.put(Material.BARRIER, "BARRIER");
        names.put(Material.BUBBLE_COLUMN, "BUBBLE_COLUMN");
        names.put(Material.CACTUS, "CACTUS");
        names.put(Material.CAKE, "CAKE");
        names.put(Material.CLOTH_DECORATION, "CARPET");
        names.put(Material.CLAY, "CLAY");
        names.put(Material.CORAL, "CORAL");
        names.put(Material.EGG, "DRAGON_EGG");
        names.put(Material.DIRT, "EARTH");
        names.put(Material.FIRE, "FIRE");
        names.put(Material.GLASS, "GLASS");
        names.put(Material.VEGETABLE, "GOURD");
        names.put(Material.ICE, "ICE");
        names.put(Material.METAL, "IRON");
        names.put(Material.LAVA, "LAVA");
        names.put(Material.LEAVES, "LEAVES");
        names.put(Material.DECORATION, "MISCELLANEOUS");
        names.put(Material.NETHER_WOOD, "NETHER_WOOD");
        names.put(Material.WATER_PLANT, "OCEAN_PLANT");
        names.put(Material.GRASS, "ORGANIC");
        names.put(Material.ICE_SOLID, "PACKED_ICE");
        names.put(Material.PISTON, "PISTON");
        names.put(Material.PLANT, "PLANTS");
        names.put(Material.PORTAL, "PORTAL");
        names.put(Material.BUILDABLE_GLASS, "REDSTONE_LIGHT");
        names.put(Material.STONE, "ROCK");
        names.put(Material.SAND, "SAND");
        names.put(Material.REPLACEABLE_WATER_PLANT, "SEA_GRASS");
        names.put(Material.SHULKER_SHELL, "SHULKER");
        names.put(Material.TOP_SNOW, "SNOW");
        names.put(Material.SNOW, "SNOW_BLOCK");
        names.put(Material.SPONGE, "SPONGE");
        names.put(Material.STRUCTURAL_AIR, "STRUCTURE_VOID");
        names.put(Material.REPLACEABLE_PLANT, "TALL_PLANTS");
        names.put(Material.EXPLOSIVE, "TNT");
        names.put(Material.WATER, "WATER");
        names.put(Material.WEB, "WEB");
        names.put(Material.WOOD, "WOOD");
        names.put(Material.WOOL, "WOOL");

        return names;
    }

    public static <T extends Comparable<T>> BlockState setPropertyValueFromString(BlockState state, Property<T> prop, String valueStr)
    {
        Optional<T> value = prop.getValue(valueStr);

        if (value.isPresent())
        {
            return state.setValue(prop, value.get());
        }

        return state;
    }

    public static List<BlockState> getFilteredStates(Collection<BlockState> initialStates, String propName, String propValue)
    {
        List<BlockState> list = new ArrayList<>();

        for (BlockState state : initialStates)
        {
            Property<?> prop = state.getBlock().getStateDefinition().getProperty(propName);

            if (prop != null)
            {
                Optional<?> value = prop.getValue(propValue);

                if (value.isPresent() && state.getValue(prop).equals(value.get()))
                {
                    list.add(state);
                }
            }
        }

        return list;
    }

    public static List<Pair<String, String>> getProperties(String blockName)
    {
        List<Pair<String, String>> props = new ArrayList<>();
        Pattern patternNameProps = Pattern.compile("(?<name>([a-z0-9_]+:)?[a-z0-9\\._]+)\\[(?<props>[a-z0-9_]+=[a-z0-9_]+(,[a-z0-9_]+=[a-z0-9_]+)*)\\]");
        Matcher matcherNameProps = patternNameProps.matcher(blockName);

        if (matcherNameProps.matches())
        {
            // name[props]
            //String name = matcherNameProps.group("name");
            String propStr = matcherNameProps.group("props");
            String[] propParts = propStr.split(",");
            Pattern patternProp = Pattern.compile("(?<prop>[a-zA-Z0-9\\._-]+)=(?<value>[a-zA-Z0-9\\._-]+)");

            for (String propPart : propParts)
            {
                Matcher matcherProp = patternProp.matcher(propPart);

                if (matcherProp.matches())
                {
                    props.add(Pair.of(matcherProp.group("prop"), matcherProp.group("value")));
                }
                else
                {
                    TellMe.logger.warn("Invalid block property '{}'", propPart);
                }
            }

            Collections.sort(props); // the properties need to be in alphabetical order

            //System.out.printf("name: %s, props: %s (propStr: %s)\n", name, String.join(",", props), propStr);
        }

        return props;
    }

    private static String getTileInfo(World world, BlockPos pos)
    {
        String teInfo;
        BlockState state = world.getBlockState(pos);
        boolean teInWorld = world.getBlockEntity(pos) != null;
        boolean shouldHaveTE = state.getBlock().hasTileEntity(state);

        if (teInWorld == shouldHaveTE)
        {
            teInfo = teInWorld ? "has a BlockEntity" : "no BlockEntity";
        }
        else
        {
            teInfo = teInWorld ? "!! is not supposed to have a BlockEntity, but there is one in the world !!" :
                                 "!! is supposed to have a BlockEntity, but there isn't one in the world !!";
        }

        return teInfo;
    }

    private static List<String> getFullBlockInfo(World world, BlockPos pos, boolean targetIsChat)
    {
        List<String> lines = new ArrayList<>();
        BlockData data = BlockData.getFor(world, pos);

        // The basic block info is always printed to chat, don't include it a second time here
        if (targetIsChat == false)
        {
            lines.add(data.toString());
        }

        BlockState state = data.state;

        @SuppressWarnings("deprecation")
        float explosionResistance = state.getBlock().getExplosionResistance();

        lines.add(String.format("Full block state: %s", state));
        lines.add(String.format("Hardness: %.4f, Explosion resistance: %.4f, Material: %s",
                state.getDestroySpeed(world, pos),
                explosionResistance,
                getMaterialName(state.getMaterial())));
        lines.add("Block class: " + state.getBlock().getClass().getName());

        if (state.getValues().size() > 0)
        {
            lines.add("BlockState properties:");

            for (Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet())
            {
                lines.add(entry.getKey().toString() + ": " + entry.getValue().toString());
            }
        }
        else
        {
            lines.add("BlockState properties: <none>");
        }

        TileEntity te = world.getBlockEntity(pos);

        if (te != null)
        {
            CompoundNBT nbt = new CompoundNBT();
            te.save(nbt);
            lines.add("BlockEntity class: " + te.getClass().getName());
            lines.add("");
            lines.add("BlockEntity NBT (from BlockEntity::write()):");
            lines.addAll((new NbtStringifierPretty(targetIsChat ? TextFormatting.GRAY.toString() : null)).getNbtLines(nbt));
        }

        return lines;
    }

    public static String getMaterialName(Material material)
    {
        return MATERIAL_NAMES.getOrDefault(material, "<unknown>");
    }

    public static void printBasicBlockInfoToChat(PlayerEntity entity, World world, BlockPos pos)
    {
        entity.displayClientMessage(BlockData.getFor(world, pos).toChatMessage(), false);
    }

    @Nullable
    public static List<String> getBlockInfoFromRayTracedTarget(World world, PlayerEntity entity, RayTraceResult trace, boolean adjacent, boolean targetIsChat)
    {
        // Ray traced to a block
        if (trace.getType() == RayTraceResult.Type.BLOCK)
        {
            BlockRayTraceResult hit = (BlockRayTraceResult) trace;
            BlockPos pos = adjacent ? hit.getBlockPos().relative(hit.getDirection()) : hit.getBlockPos();
            BlockInfo.printBasicBlockInfoToChat(entity, world, pos);

            return getFullBlockInfo(world, pos, targetIsChat);
        }

        return null;
    }

    public static String blockStateToString(BlockState state)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString());

        if (state.getValues().isEmpty() == false)
        {
            sb.append('[');
            sb.append(state.getValues().entrySet().stream().map(PROPERTY_MAP_PRINTER).collect(Collectors.joining(",")));
            sb.append(']');
        }

        return sb.toString();
    }

    public static final Function<Entry<Property<?>, Comparable<?>>, String> PROPERTY_MAP_PRINTER = new Function<Map.Entry<Property<?>, Comparable<?>>, String>()
    {
        @Override
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry)
        {
            if (entry == null)
            {
                return "<NULL>";
            }
            else
            {
                Property<?> property = entry.getKey();
                return property.getName() + "=" + this.valueToString(property, entry.getValue());
            }
        }

        @SuppressWarnings("unchecked")
        private <T extends Comparable<T>> String valueToString(Property<T> property, Object value)
        {
            return property.getName((T) value);
        }
    };

    public static boolean statePassesFilter(BlockState state, Map<Property<?>, Comparable<?>> filterProperties)
    {
        for (Property<?> prop : state.getProperties())
        {
            if (filterProperties.containsKey(prop) &&
                filterProperties.get(prop).equals(state.getValue(prop)) == false)
            {
                return false;
            }
        }

        return true;
    }

    public static class BlockData
    {
        private final BlockState state;
        private final String regName;
        private final String displayName;
        private final String teInfo;

        public BlockData(BlockState state, String displayName, String regName, String teInfo)
        {
            this.state = state;
            this.displayName = displayName;
            this.regName = regName;
            this.teInfo = teInfo;
        }

        public static BlockData getFor(World world, BlockPos pos)
        {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            @SuppressWarnings("deprecation")
            ItemStack stack = block.getCloneItemStack(world, pos, state);
            ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
            String registryName = rl != null ? rl.toString() : "<null>";
            String displayName;

            if (stack.isEmpty() == false)
            {
                displayName = stack.getHoverName().getString();
            }
            // Blocks that are not obtainable/don't have an ItemBlock
            else
            {
                displayName = (new TranslationTextComponent(block.getDescriptionId())).getString();
            }

            return new BlockData(state, displayName, registryName, getTileInfo(world, pos));
        }

        public ITextComponent toChatMessage()
        {
            String textPre = String.format("%s (", this.displayName);
            String textPost = String.format(") %s", this.teInfo);

            return OutputUtils.getClipboardCopiableMessage(textPre, this.regName, textPost);
        }

        @Override
        public String toString()
        {
            return String.format("%s (%s) %s", this.displayName, this.regName, this.teInfo);
        }
    }

    public static String getBlockEntityNameFor(TileEntityType<?> type)
    {
        ResourceLocation id = TileEntityType.getKey(type);
        return id != null ? id.toString() : "<null>";
    }
}
