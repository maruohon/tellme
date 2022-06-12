package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Optional;
import com.google.common.collect.UnmodifiableIterator;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import fi.dy.masa.malilib.util.game.wrap.ItemWrap;
import fi.dy.masa.tellme.LiteModTellMe;
import fi.dy.masa.tellme.command.SubCommand;
import fi.dy.masa.tellme.datadump.DataDump;

public class BlockInfo
{
    public static <T extends Comparable<T>> IBlockState setPropertyValueFromString(IBlockState state, IProperty<T> prop, String valueStr)
    {
        Optional<T> value = prop.parseValue(valueStr);

        if (value.isPresent())
        {
            return state.withProperty(prop, value.get());
        }

        return state;
    }

    public static <T extends Comparable<T>> List<IBlockState> getFilteredStates(Collection<IBlockState> initialStates, String propName, String propValue)
    {
        List<IBlockState> list = new ArrayList<>();

        for (IBlockState state : initialStates)
        {
            @SuppressWarnings("unchecked")
            IProperty<T> prop = (IProperty<T>) state.getBlock().getBlockState().getProperty(propName);

            if (prop != null)
            {
                Optional<T> value = prop.parseValue(propValue);

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

            for (int i = 0; i < propParts.length; i++)
            {
                Matcher matcherProp = patternProp.matcher(propParts[i]);

                if (matcherProp.matches())
                {
                    props.add(Pair.of(matcherProp.group("prop"), matcherProp.group("value")));
                }
                else
                {
                    LiteModTellMe.logger.warn("Invalid block property '{}'", propParts[i]);
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
        IBlockState state = world.getBlockState(pos).getActualState(world, pos);
        boolean teInWorld = world.getTileEntity(pos) != null;
        boolean shouldHaveTE = state.getBlock().hasTileEntity();

        if (teInWorld == shouldHaveTE)
        {
            teInfo = teInWorld ? "has a TileEntity" : "no TileEntity";
        }
        else
        {
            teInfo = teInWorld ? "!! is not supposed to have a TileEntity, but there is one in the world !!" :
                                 "!! is supposed to have a TileEntity, but there isn't one in the world !!";
        }

        return teInfo;
    }

    private static List<String> getFullBlockInfo(EntityPlayer player, World world, BlockPos pos)
    {
        List<String> lines = new ArrayList<>();
        BlockData data = BlockData.getFor(world, pos, player);
        lines.add(data.toString());

        IBlockState state = data.actualState;

        lines.add(String.format("Full block state: %s", state));
        lines.add(String.format("Hardness: %.4f, Resistance: %.4f, Material: %s",
                state.getBlockHardness(world, pos),
                state.getBlock().getExplosionResistance(player) * 5f,
                getMaterialName(state.getMaterial())));
        lines.add("Block class: " + state.getBlock().getClass().getName());

        if (state.getProperties().size() > 0)
        {
            lines.add("IBlockState properties, including getActualState():");

            UnmodifiableIterator<Entry<IProperty<?>, Comparable<?>>> iter = state.getProperties().entrySet().iterator();

            while (iter.hasNext() == true)
            {
                Entry<IProperty<?>, Comparable<?>> entry = iter.next();
                lines.add(entry.getKey().toString() + ": " + entry.getValue().toString());
            }
        }
        else
        {
            lines.add("IBlockState properties: <none>");
        }

        TileEntity te = world.getTileEntity(pos);

        if (te != null)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            te.writeToNBT(nbt);
            lines.add("TileEntity class: " + te.getClass().getName());
            lines.add("");
            lines.add("TileEntity NBT (from TileEntity#writeToNBT()):");
            NBTFormatter.getPrettyFormattedNBT(lines, nbt);
        }

        return lines;
    }

    public static String getMaterialName(Material material)
    {
        if (material == Material.AIR)           { return "AIR";         }
        if (material == Material.GRASS)         { return "GRASS";       }
        if (material == Material.GROUND)        { return "GROUND";      }
        if (material == Material.WOOD)          { return "WOOD";        }
        if (material == Material.ROCK)          { return "ROCK";        }
        if (material == Material.IRON)          { return "IRON";        }
        if (material == Material.ANVIL)         { return "ANVIL";       }
        if (material == Material.WATER)         { return "WATER";       }
        if (material == Material.LAVA)          { return "LAVA";        }
        if (material == Material.LEAVES)        { return "LEAVES";      }
        if (material == Material.PLANTS)        { return "PLANTS";      }
        if (material == Material.VINE)          { return "VINE";        }
        if (material == Material.SPONGE)        { return "SPONGE";      }
        if (material == Material.CLOTH)         { return "CLOTH";       }
        if (material == Material.FIRE)          { return "FIRE";        }
        if (material == Material.SAND)          { return "SAND";        }
        if (material == Material.CIRCUITS)      { return "CIRCUITS";    }
        if (material == Material.CARPET)        { return "CARPET";      }
        if (material == Material.GLASS)         { return "GLASS";       }
        if (material == Material.REDSTONE_LIGHT){ return "REDSTONE_LIGHT"; }
        if (material == Material.TNT)           { return "TNT";         }
        if (material == Material.CORAL)         { return "CORAL";       }
        if (material == Material.ICE)           { return "ICE";         }
        if (material == Material.PACKED_ICE)    { return "PACKED_ICE";  }
        if (material == Material.SNOW)          { return "SNOW";        }
        if (material == Material.CRAFTED_SNOW)  { return "CRAFTED_SNOW";}
        if (material == Material.CACTUS)        { return "CACTUS";      }
        if (material == Material.CLAY)          { return "CLAY";        }
        if (material == Material.GOURD)         { return "GOURD";       }
        if (material == Material.DRAGON_EGG)    { return "DRAGON_EGG";  }
        if (material == Material.PORTAL)        { return "PORTAL";      }
        if (material == Material.CAKE)          { return "CAKE";        }
        if (material == Material.WEB)           { return "WEB";         }
        if (material == Material.PISTON)        { return "PISTON";      }
        if (material == Material.BARRIER)       { return "BARRIER";     }
        if (material == Material.STRUCTURE_VOID){ return "STRUCTURE_VOID"; }

        return "unknown";
    }

    public static void printBasicBlockInfoToChat(EntityPlayer player, World world, BlockPos pos)
    {
        player.sendMessage(BlockData.getFor(world, pos, player).toChatMessage());
    }

    public static void printBlockInfoToConsole(EntityPlayer player, World world, BlockPos pos)
    {
        List<String> lines = getFullBlockInfo(player, world, pos);

        for (String line : lines)
        {
            LiteModTellMe.logger.info(line);
        }
    }

    public static void dumpBlockInfoToFile(EntityPlayer player, World world, BlockPos pos)
    {
        File file = DataDump.dumpDataToFile("block_and_tileentity_data", getFullBlockInfo(player, world, pos));
        SubCommand.sendClickableLinkMessage(player, "Output written to file %s", file);
    }

    public static void getBlockInfoFromRayTracedTarget(World world, EntityPlayer player, RayTraceResult trace, boolean adjacent, boolean dumpToFile)
    {
        // Ray traced to a block
        if (trace.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockPos pos = adjacent ? trace.getBlockPos().offset(trace.sideHit) : trace.getBlockPos();
            BlockInfo.printBasicBlockInfoToChat(player, world, pos);

            if (dumpToFile)
            {
                dumpBlockInfoToFile(player, world, pos);
            }
            else
            {
                printBlockInfoToConsole(player, world, pos);
            }
        }
    }

    public static class BlockData
    {
        private final IBlockState actualState;
        private final String regName;
        private final int id;
        private final int meta;
        private final String displayName;
        private final String teInfo;

        public BlockData(IBlockState actualState, String displayName, String regName, int id, int meta, String teInfo)
        {
            this.actualState = actualState;
            this.displayName = displayName;
            this.regName = regName;
            this.id = id;
            this.meta = meta;
            this.teInfo = teInfo;
        }

        public static BlockData getFor(World world, BlockPos pos, EntityPlayer player)
        {
            IBlockState actualState = world.getBlockState(pos).getActualState(world, pos);
            Block block = actualState.getBlock();

            int id = Block.getIdFromBlock(block);
            int meta = block.getMetaFromState(actualState);
            //ItemStack stack = block.getPickBlock(state, RayTraceUtils.getRayTraceFromEntity(world, player, true), world, pos, player);
            //ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));
            //ItemStack stack = new ItemStack(block, 1, block.getDamageValue(world, pos));
            ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
            ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(actualState));
            String registryName = rl != null ? rl.toString() : "<null>";
            String displayName;

            if (ItemWrap.notEmpty(stack))
            {
                displayName = stack.getDisplayName();
            }
            // Blocks that are not obtainable/don't have an ItemBlock
            else
            {
                displayName = registryName;
            }

            return new BlockData(actualState, displayName, registryName, id, meta, getTileInfo(world, pos));
        }

        public ITextComponent toChatMessage()
        {
            String copyStr = this.meta != 0 ? this.regName + ":" + this.meta : this.regName;
            String textPre = String.format("%s (", this.displayName);
            String textPost = String.format(" - %d:%d) %s", this.id, this.meta, this.teInfo);

            return ChatUtils.getClipboardCopiableMessage(textPre, copyStr, textPost);
        }

        @Override
        public String toString()
        {
            return String.format("%s (%s - %d:%d) %s", this.displayName, this.regName, this.id, this.meta, this.teInfo);
        }
    }
}
