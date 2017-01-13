package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.Block;
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

        if (stack != null && stack.getItem() != null)
        {
            dname = stack.getDisplayName();
        }
        // Blocks that are not obtainable/don't have an ItemBlock
        else
        {
            dname = name;
        }

        if (block.hasTileEntity(iBlockState))
        {
            lines.add(String.format("%s (%s - %d:%d) has a TileEntity", dname, name, id, meta));
        }
        else
        {
            lines.add(String.format("%s (%s - %d:%d) no TileEntity", dname, name, id, meta));
        }

        return lines;
    }

    private static List<String> getFullBlockInfo(EntityPlayer player, World world, BlockPos pos)
    {
        List<String> lines = getBasicBlockInfo(player, world, pos);
        IBlockState state = world.getBlockState(pos).getActualState(world, pos);

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

    public static void getBlockInfoFromRayTracedTarget(World world, EntityPlayer player)
    {
        getBlockInfoFromRayTracedTarget(world, player, player.isSneaking());
    }

    public static void getBlockInfoFromRayTracedTarget(World world, EntityPlayer player, boolean dumpToFile)
    {
        // Ray tracing to be able to target fluid blocks, although currently it doesn't work for non-source blocks
        RayTraceResult mop = RayTraceUtils.rayTraceFromPlayer(world, player, true);
        BlockPos pos;

        // Ray traced to a block
        if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            pos = mop.getBlockPos();
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
