package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.nbt.NbtStringifierPretty;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockInfo
{
    private static final Map<Material, String> MATERIAL_NAMES = getMaterialNames();

    private static Map<Material, String> getMaterialNames()
    {
        Map<Material, String> names = new HashMap<>();

        names.put(Material.AIR, "AIR");
        names.put(Material.ANVIL, "ANVIL");
        names.put(Material.BAMBOO, "BAMBOO");
        names.put(Material.BAMBOO_SAPLING, "BAMBOO_SAPLING");
        names.put(Material.BARRIER, "BARRIER");
        names.put(Material.BUBBLE_COLUMN, "BUBBLE_COLUMN");
        names.put(Material.CACTUS, "CACTUS");
        names.put(Material.CAKE, "CAKE");
        names.put(Material.CARPET, "CARPET");
        names.put(Material.CLAY, "CLAY");
        names.put(Material.CORAL, "CORAL");
        names.put(Material.DRAGON_EGG, "DRAGON_EGG");
        names.put(Material.EARTH, "EARTH");
        names.put(Material.FIRE, "FIRE");
        names.put(Material.GLASS, "GLASS");
        names.put(Material.GOURD, "GOURD");
        names.put(Material.ICE, "ICE");
        names.put(Material.IRON, "IRON");
        names.put(Material.LAVA, "LAVA");
        names.put(Material.LEAVES, "LEAVES");
        names.put(Material.MISCELLANEOUS, "MISCELLANEOUS");
        names.put(Material.OCEAN_PLANT, "OCEAN_PLANT");
        names.put(Material.ORGANIC, "ORGANIC");
        names.put(Material.PACKED_ICE, "PACKED_ICE");
        names.put(Material.PISTON, "PISTON");
        names.put(Material.PLANTS, "PLANTS");
        names.put(Material.PORTAL, "PORTAL");
        names.put(Material.REDSTONE_LIGHT, "REDSTONE_LIGHT");
        names.put(Material.ROCK, "ROCK");
        names.put(Material.SAND, "SAND");
        names.put(Material.SEA_GRASS, "SEA_GRASS");
        names.put(Material.SHULKER, "SHULKER");
        names.put(Material.SNOW, "SNOW");
        names.put(Material.SNOW_BLOCK, "SNOW_BLOCK");
        names.put(Material.SPONGE, "SPONGE");
        names.put(Material.STRUCTURE_VOID, "STRUCTURE_VOID");
        names.put(Material.TALL_PLANTS, "TALL_PLANTS");
        names.put(Material.TNT, "TNT");
        names.put(Material.WATER, "WATER");
        names.put(Material.WEB, "WEB");
        names.put(Material.WOOD, "WOOD");
        names.put(Material.WOOL, "WOOL");

        return names;
    }

    public static <T extends Comparable<T>> BlockState setPropertyValueFromString(BlockState state, IProperty<T> prop, String valueStr)
    {
        Optional<T> value = prop.parseValue(valueStr);

        if (value.isPresent())
        {
            return state.with(prop, value.get());
        }

        return state;
    }

    public static <T extends Comparable<T>> List<BlockState> getFilteredStates(Collection<BlockState> initialStates, String propName, String propValue)
    {
        List<BlockState> list = new ArrayList<>();

        for (BlockState state : initialStates)
        {
            IProperty<?> prop = state.getBlock().getStateContainer().getProperty(propName);

            if (prop != null)
            {
                Optional<?> value = prop.parseValue(propValue);

                if (value.isPresent() && state.get(prop).equals(value.get()))
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

            for (int i = 0; i < propParts.length; i++)
            {
                Matcher matcherProp = patternProp.matcher(propParts[i]);

                if (matcherProp.matches())
                {
                    props.add(Pair.of(matcherProp.group("prop"), matcherProp.group("value")));
                }
                else
                {
                    TellMe.logger.warn("Invalid block property '{}'", propParts[i]);
                }
            }

            Collections.sort(props); // the properties need to be in alphabetical order

            //System.out.printf("name: %s, props: %s (propStr: %s)\n", name, String.join(",", props), propStr);
        }

        return props;
    }

    private static String getTileInfo(World world, BlockPos pos)
    {
        String teInfo = "";
        BlockState state = world.getBlockState(pos);
        boolean teInWorld = world.getTileEntity(pos) != null;
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

        lines.add(String.format("Full block state: %s", state));
        lines.add(String.format("Hardness: %.4f, Explosion resistance: %.4f, Material: %s",
                state.getBlockHardness(world, pos),
                state.getBlock().getExplosionResistance(state, world, pos, null, new Explosion(world, null, pos.getX(), pos.getY(), pos.getZ(), 2, false, Explosion.Mode.NONE)),
                getMaterialName(state.getMaterial())));
        lines.add("Block class: " + state.getBlock().getClass().getName());

        if (state.getProperties().size() > 0)
        {
            lines.add("BlockState properties:");

            UnmodifiableIterator<Entry<IProperty<?>, Comparable<?>>> iter = state.getValues().entrySet().iterator();

            while (iter.hasNext())
            {
                Entry<IProperty<?>, Comparable<?>> entry = iter.next();
                lines.add(entry.getKey().toString() + ": " + entry.getValue().toString());
            }
        }
        else
        {
            lines.add("BlockState properties: <none>");
        }

        getExtendedBlockStateInfo(world, state, pos, lines);

        TileEntity te = world.getTileEntity(pos);

        if (te != null)
        {
            CompoundNBT nbt = new CompoundNBT();
            te.write(nbt);
            lines.add("BlockEntity class: " + te.getClass().getName());
            lines.add("");
            lines.add("BlockEntity NBT (from BlockEntity::write()):");
            lines.addAll((new NbtStringifierPretty(targetIsChat ? TextFormatting.GRAY.toString() : null)).getNbtLines(nbt));
        }

        return lines;
    }

    private static void getExtendedBlockStateInfo(IBlockReader world, BlockState state, BlockPos pos, List<String> lines)
    {
        try
        {
            state = state.getExtendedState(world, pos);

            /*
            if (state instanceof IExtendedBlockState)
            {
                IExtendedBlockState extendedState = (IExtendedBlockState) state;

                if (extendedState.getUnlistedProperties().size() > 0)
                {
                    lines.add("IExtendedBlockState properties:");

                    UnmodifiableIterator<Entry<IUnlistedProperty<?>, Optional<?>>> iterExt = extendedState.getUnlistedProperties().entrySet().iterator();

                    while (iterExt.hasNext())
                    {
                        Entry<IUnlistedProperty<?>, Optional<?>> entry = iterExt.next();
                        lines.add(MoreObjects.toStringHelper(entry.getKey())
                                .add("name", entry.getKey().getName())
                                .add("clazz", entry.getKey().getType())
                                .add("value", entry.getValue().toString()).toString());
                    }
                }
            }
            */
        }
        catch (Exception e)
        {
            TellMe.logger.error("getFullBlockInfo(): Exception while calling getExtendedState() on the block {}", state);
        }
    }

    public static String getMaterialName(Material material)
    {
        return MATERIAL_NAMES.getOrDefault(material, "<unknown>");
    }

    public static void printBasicBlockInfoToChat(Entity entity, World world, BlockPos pos)
    {
        entity.sendMessage(BlockData.getFor(world, pos).toChatMessage());
    }

    @Nullable
    public static List<String> getBlockInfoFromRayTracedTarget(World world, Entity entity, RayTraceResult trace, boolean adjacent, boolean targetIsChat)
    {
        // Ray traced to a block
        if (trace.getType() == RayTraceResult.Type.BLOCK)
        {
            BlockRayTraceResult hit = (BlockRayTraceResult) trace;
            BlockPos pos = adjacent ? hit.getPos().offset(hit.getFace()) : hit.getPos();
            BlockInfo.printBasicBlockInfoToChat(entity, world, pos);

            return getFullBlockInfo(world, pos, targetIsChat);
        }

        return null;
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
            ItemStack stack = block.getItem(world, pos, state);
            ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
            String registryName = rl != null ? rl.toString() : "<null>";
            String displayName;

            if (stack.isEmpty() == false)
            {
                displayName = stack.getDisplayName().getString();
            }
            // Blocks that are not obtainable/don't have an ItemBlock
            else
            {
                displayName = registryName;
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
        ResourceLocation id = TileEntityType.getId(type);
        return id != null ? id.toString() : "<null>";
    }
}
