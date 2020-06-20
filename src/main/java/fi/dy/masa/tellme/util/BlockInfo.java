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
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.nbt.NbtStringifierPretty;

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
        names.put(Material.COBWEB, "COBWEB");
        names.put(Material.EARTH, "EARTH");
        names.put(Material.EGG, "EGG");
        names.put(Material.FIRE, "FIRE");
        names.put(Material.GLASS, "GLASS");
        names.put(Material.ICE, "ICE");
        names.put(Material.LAVA, "LAVA");
        names.put(Material.LEAVES, "LEAVES");
        names.put(Material.METAL, "METAL");
        names.put(Material.ORGANIC, "ORGANIC");
        names.put(Material.PACKED_ICE, "PACKED_ICE");
        names.put(Material.PART, "PART");
        names.put(Material.PISTON, "PISTON");
        names.put(Material.PLANT, "PLANT");
        names.put(Material.PORTAL, "PORTAL");
        names.put(Material.PUMPKIN, "PUMPKIN");
        names.put(Material.REDSTONE_LAMP, "REDSTONE_LAMP");
        names.put(Material.REPLACEABLE_PLANT, "REPLACEABLE_PLANT");
        names.put(Material.SAND, "SAND");
        names.put(Material.SEAGRASS, "SEAGRASS");
        names.put(Material.SHULKER_BOX, "SHULKER_BOX");
        names.put(Material.SNOW, "SNOW");
        names.put(Material.SNOW_BLOCK, "SNOW_BLOCK");
        names.put(Material.SPONGE, "SPONGE");
        names.put(Material.STONE, "STONE");
        names.put(Material.STRUCTURE_VOID, "STRUCTURE_VOID");
        names.put(Material.TNT, "TNT");
        names.put(Material.UNDERWATER_PLANT, "UNDERWATER_PLANT");
        names.put(Material.UNUSED_PLANT, "UNUSED_PLANT");
        names.put(Material.WATER, "WATER");
        names.put(Material.WOOD, "WOOD");
        names.put(Material.WOOL, "WOOL");

        return names;
    }

    public static <T extends Comparable<T>> BlockState setPropertyValueFromString(BlockState state, Property<T> prop, String valueStr)
    {
        Optional<T> value = prop.getValue(valueStr);

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
            Property<?> prop = state.getBlock().getStateFactory().getProperty(propName);

            if (prop != null)
            {
                Optional<?> value = prop.getValue(propValue);

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
        boolean teInWorld = world.getBlockEntity(pos) != null;
        boolean shouldHaveTE = state.getBlock().hasBlockEntity();

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
                state.getHardness(world, pos),
                state.getBlock().getBlastResistance(),
                getMaterialName(state.getMaterial())));
        lines.add("Block class: " + state.getBlock().getClass().getName());

        if (state.getProperties().size() > 0)
        {
            lines.add("BlockState properties:");

            UnmodifiableIterator<Entry<Property<?>, Comparable<?>>> iter = state.getEntries().entrySet().iterator();

            while (iter.hasNext())
            {
                Entry<Property<?>, Comparable<?>> entry = iter.next();
                lines.add(entry.getKey().toString() + ": " + entry.getValue().toString());
            }
        }
        else
        {
            lines.add("BlockState properties: <none>");
        }

        BlockEntity te = world.getBlockEntity(pos);

        if (te != null)
        {
            CompoundTag nbt = new CompoundTag();
            te.toTag(nbt);
            lines.add("BlockEntity class: " + te.getClass().getName());
            lines.add("");
            lines.add("BlockEntity NBT (from BlockEntity::write()):");
            lines.addAll((new NbtStringifierPretty(targetIsChat ? Formatting.GRAY.toString() : null)).getNbtLines(nbt));
        }

        return lines;
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
    public static List<String> getBlockInfoFromRayTracedTarget(World world, Entity entity, HitResult trace, boolean adjacent, boolean targetIsChat)
    {
        // Ray traced to a block
        if (trace.getType() == HitResult.Type.BLOCK)
        {
            BlockHitResult hit = (BlockHitResult) trace;
            BlockPos pos = adjacent ? hit.getBlockPos().offset(hit.getSide()) : hit.getBlockPos();
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

            ItemStack stack = new ItemStack(block.asItem());
            Identifier rl = Registry.BLOCK.getId(block);
            String registryName = rl != null ? rl.toString() : "<null>";
            String displayName;

            if (stack.isEmpty() == false)
            {
                displayName = stack.getName().getString();
            }
            // Blocks that are not obtainable/don't have an ItemBlock
            else
            {
                displayName = (new TranslatableText(block.getTranslationKey())).getString();
            }

            return new BlockData(state, displayName, registryName, getTileInfo(world, pos));
        }

        public Text toChatMessage()
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

    public static String getBlockEntityNameFor(BlockEntityType<?> type)
    {
        Identifier id = BlockEntityType.getId(type);
        return id != null ? id.toString() : "<null>";
    }
}
