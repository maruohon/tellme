package fi.dy.masa.tellme.util;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;

public class BlockInfo
{
    public static ArrayList<String> getBasicBlockInfo(EntityPlayer player, World world, BlockPos pos)
    {
        ArrayList<String> lines = new ArrayList<String>();

        if (world == null)
        {
            return lines;
        }

        IBlockState iBlockState = world.getBlockState(pos);
        Block block = iBlockState.getBlock();

        int id = Block.getIdFromBlock(block);
        int meta = block.getMetaFromState(iBlockState);
        ItemStack stack = new ItemStack(block, 1, block.damageDropped(iBlockState));
        //String name = GameRegistry.findUniqueIdentifierFor(block).toString();
        String name = Block.blockRegistry.getNameForObject(block).toString();
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

        String fmt = "%s (%s - %d:%d) %s";
        lines.add(String.format(fmt, dname, name, id, meta, teInfo));

        return lines;
    }

    public static ArrayList<String> getFullBlockInfo(EntityPlayer player, World world, BlockPos pos)
    {
        ArrayList<String> lines = getBasicBlockInfo(player, world, pos);

        TileEntity te = world.getTileEntity(pos);
        if (te != null)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            te.writeToNBT(nbt);
            lines.add("");
            NBTFormatter.NBTFormatterPretty(lines, nbt);
        }

        return lines;
    }

    public static void printBasicBlockInfoToChat(EntityPlayer player, World world, BlockPos pos)
    {
        for (String line : getBasicBlockInfo(player, world, pos))
        {
            player.addChatMessage(new ChatComponentText(line));
        }
    }

    public static void printBlockInfoToConsole(EntityPlayer player, World world, BlockPos pos)
    {
        ArrayList<String> lines = getFullBlockInfo(player, world, pos);

        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void dumpBlockInfoToFile(EntityPlayer player, World world, BlockPos pos)
    {
        DataDump.dumpDataToFile("block_and_tileentity_data", getFullBlockInfo(player, world, pos));
    }
}
