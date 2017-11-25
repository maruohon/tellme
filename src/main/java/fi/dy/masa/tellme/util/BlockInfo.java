package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.SubCommand;
import fi.dy.masa.tellme.datadump.DataDump;

public class BlockInfo
{
    private static String getTileInfo(World world, BlockPos pos)
    {
        String teInfo = "";
        IBlockState state = world.getBlockState(pos).getActualState(world, pos);
        boolean teInWorld = world.getTileEntity(pos) != null;
        boolean shouldHaveTE = state.getBlock().hasTileEntity(state);

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

    @SuppressWarnings("deprecation")
    private static List<String> getFullBlockInfo(EntityPlayer player, World world, BlockPos pos)
    {
        List<String> lines = new ArrayList<>();
        lines.add(BlockData.getFor(world, pos, player).toString() + " " + getTileInfo(world, pos));

        IBlockState state = world.getBlockState(pos).getActualState(world, pos);

        lines.add(String.format("Full block state: %s", state.toString()));
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

        TellMe.proxy.getExtendedBlockStateInfo(world, state, pos, lines);

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
            TellMe.logger.info(line);
        }
    }

    public static void dumpBlockInfoToFile(EntityPlayer player, World world, BlockPos pos)
    {
        File file = DataDump.dumpDataToFile("block_and_tileentity_data", getFullBlockInfo(player, world, pos));
        SubCommand.sendClickableLinkMessage(player, "Output written to file %s", file);
    }

    public static void getBlockInfoFromRayTracedTarget(World world, EntityPlayer player, RayTraceResult trace, boolean adjacent)
    {
        getBlockInfoFromRayTracedTarget(world, player, trace, adjacent, player.isSneaking());
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
        private final String regName;
        private final int id;
        private final int meta;
        private final String displayName;
        private final String teInfo;

        public BlockData(String displayName, String regName, int id, int meta, String teInfo)
        {
            this.displayName = displayName;
            this.regName = regName;
            this.id = id;
            this.meta = meta;
            this.teInfo = teInfo;
        }

        public static BlockData getFor(World world, BlockPos pos, EntityPlayer player)
        {
            IBlockState state = world.getBlockState(pos).getActualState(world, pos);
            Block block = state.getBlock();

            int id = Block.getIdFromBlock(block);
            int meta = block.getMetaFromState(state);
            ItemStack stack = block.getPickBlock(state, RayTraceUtils.getRayTraceFromEntity(world, player, true), world, pos, player);
            //ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));
            //ItemStack stack = new ItemStack(block, 1, block.getDamageValue(world, pos));
            String registryName = ForgeRegistries.BLOCKS.getKey(block).toString();
            String displayName;

            if (stack.isEmpty() == false)
            {
                displayName = stack.getDisplayName();
            }
            // Blocks that are not obtainable/don't have an ItemBlock
            else
            {
                displayName = registryName;
            }

            return new BlockData(displayName, registryName, id, meta, getTileInfo(world, pos));
        }

        public ITextComponent toChatMessage()
        {
            String copyStr = this.meta != 0 ? this.regName + ":" + this.meta : this.regName;

            TextComponentString copy = new TextComponentString(this.regName);
            copy.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellme copy-to-clipboard " + copyStr));
            copy.getStyle().setUnderlined(Boolean.valueOf(true));

            TextComponentString hoverText = new TextComponentString(String.format("Copy the string '%s' to clipboard", copyStr));
            copy.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

            TextComponentString full = new TextComponentString(String.format("%s (", this.displayName));
            full.appendSibling(copy).appendText(String.format(" - %d:%d) %s", this.id, this.meta, this.teInfo));

            return full;
        }

        @Override
        public String toString()
        {
            return String.format("%s (%s - %d:%d) %s", this.displayName, this.regName, this.id, this.meta, this.teInfo);
        }
    }
}
