package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import com.google.common.base.Optional;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import fi.dy.masa.tellme.TellMe;

public class BlockInfo
{
    public static List<String> getBasicBlockInfo(EntityPlayer player, World world, BlockPos pos)
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
        String name = Block.REGISTRY.getNameForObject(block).toString();
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

        String teInfo;
        if (block.hasTileEntity(iBlockState) == true)
        {
            teInfo = "has a TE";
        }
        else
        {
            teInfo = "no TE";
        }

        lines.add(String.format("%s (%s - %d:%d) %s", dname, name, id, meta, teInfo));

        return lines;
    }

    public static List<String> getFullBlockInfo(EntityPlayer player, World world, BlockPos pos)
    {
        List<String> lines = getBasicBlockInfo(player, world, pos);

        IBlockState iBlockState = world.getBlockState(pos);
        try
        {
            iBlockState = iBlockState.getActualState(world, pos);
            iBlockState = iBlockState.getBlock().getExtendedState(iBlockState, world, pos);
        }
        catch (Exception e)
        {
            TellMe.logger.error("getFullBlockInfo(): Exception while calling getActualState() or getExtendedState() on the block");
        }

        lines.add("IBlockState properties, including getActualState():");

        UnmodifiableIterator<Entry<IProperty<?>, Comparable<?>>> iter = iBlockState.getProperties().entrySet().iterator();

        while (iter.hasNext() == true)
        {
            Entry<IProperty<?>, Comparable<?>> entry = iter.next();
            lines.add(entry.getKey().toString() + ": " + entry.getValue().toString());
        }

        if (iBlockState instanceof IExtendedBlockState)
        {
            lines.add("IExtendedBlockState properties:");

            IExtendedBlockState extendedState = (IExtendedBlockState)iBlockState;
            UnmodifiableIterator<Entry<IUnlistedProperty<?>, Optional<?>>> iterExt = extendedState.getUnlistedProperties().entrySet().iterator();

            while (iterExt.hasNext() == true)
            {
                Entry<IUnlistedProperty<?>, Optional<?>> entry = iterExt.next();
                lines.add(entry.getKey().toString() + ": " + entry.getValue().toString());
            }
        }

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
            player.addChatMessage(new TextComponentString(line));
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
        player.addChatMessage(new TextComponentString("Output written to file " + f.getName()));
    }
}
