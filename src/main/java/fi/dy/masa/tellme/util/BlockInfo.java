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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;

public class BlockInfo
{
    private static List<String> getBasicBlockInfo(EntityPlayer player, World world, BlockPos pos)
    {
        List<String> lines = new ArrayList<String>();

        if (world == null)
        {
            return lines;
        }

        IBlockState iBlockState = world.getBlockState(pos);
        iBlockState = iBlockState.getActualState(world, pos);
        Block block = iBlockState.getBlock();

        int id = Block.getIdFromBlock(block);
        int meta = block.getMetaFromState(iBlockState);
        ItemStack stack = new ItemStack(block, 1, block.damageDropped(iBlockState));
        //ItemStack stack = new ItemStack(block, 1, block.getDamageValue(world, pos));
        String name = ForgeRegistries.BLOCKS.getKey(block).toString();
        String dname;

        if (stack.isEmpty() == false)
        {
            dname = stack.getDisplayName();
        }
        // Blocks that are not obtainable/don't have an ItemBlock
        else
        {
            dname = name;
        }

        boolean teInWorld = world.getTileEntity(pos) != null;
        boolean shouldHaveTE = block.hasTileEntity(iBlockState);

        if (teInWorld == shouldHaveTE)
        {
            if (teInWorld)
            {
                lines.add(String.format("%s (%s - %d:%d) has a TileEntity", dname, name, id, meta));
            }
            else
            {
                lines.add(String.format("%s (%s - %d:%d) no TileEntity", dname, name, id, meta));
            }
        }
        else
        {
            if (teInWorld)
            {
                lines.add(String.format("%s (%s - %d:%d) !! is not supposed to have a TileEntity, but there is one in the world !!",
                        dname, name, id, meta));
            }
            else
            {
                lines.add(String.format("%s (%s - %d:%d) !! is supposed to have a TileEntity, but there isn't one in the world !!",
                        dname, name, id, meta));
            }
        }

        return lines;
    }

    @SuppressWarnings("deprecation")
    private static List<String> getFullBlockInfo(EntityPlayer player, World world, BlockPos pos)
    {
        List<String> lines = getBasicBlockInfo(player, world, pos);
        IBlockState state = world.getBlockState(pos).getActualState(world, pos);

        lines.add(String.format("Hardness: %.4f, Resistance: %.4f, Material: %s",
                state.getBlockHardness(world, pos),
                state.getBlock().getExplosionResistance(player) * 5f,
                getMaterialName(state.getMaterial())));
        lines.add("IBlockState properties, including getActualState():");

        UnmodifiableIterator<Entry<IProperty<?>, Comparable<?>>> iter = state.getProperties().entrySet().iterator();

        while (iter.hasNext() == true)
        {
            Entry<IProperty<?>, Comparable<?>> entry = iter.next();
            lines.add(entry.getKey().toString() + ": " + entry.getValue().toString());
        }

        TellMe.proxy.getExtendedBlockStateInfo(world, state, pos, lines);

        TileEntity te = world.getTileEntity(pos);
        if (te != null)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            te.writeToNBT(nbt);
            lines.add("");
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
        for (String line : getBasicBlockInfo(player, world, pos))
        {
            player.sendMessage(new TextComponentString(line));
        }
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
        File f = DataDump.dumpDataToFile("block_and_tileentity_data", getFullBlockInfo(player, world, pos));
        player.sendMessage(new TextComponentString("Output written to file " + f.getName()));
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
                BlockInfo.dumpBlockInfoToFile(player, world, pos);
            }
            else
            {
                BlockInfo.printBlockInfoToConsole(player, world, pos);
            }
        }
    }
}
