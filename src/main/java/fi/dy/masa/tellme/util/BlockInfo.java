package fi.dy.masa.tellme.util;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;

public class BlockInfo
{
    public static void printBasicBlockInfoToChat(EntityPlayer player, World world, int x, int y, int z)
    {
        for (String line : getBasicBlockInfo(player, world, x, y, z))
        {
            player.addChatMessage(new ChatComponentText(line));
        }
    }

    public static ArrayList<String> getBasicBlockInfo(EntityPlayer player, World world, int x, int y, int z)
    {
        ArrayList<String> lines = new ArrayList<String>();

        if (world == null)
        {
            return lines;
        }

        Block block = world.getBlock(x, y, z);

        int id = Block.getIdFromBlock(block);
        int meta = world.getBlockMetadata(x, y, z);
        ItemStack stack = new ItemStack(block, 1, block.damageDropped(meta));
        String name = Block.blockRegistry.getNameForObject(block);
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

        String fmt = "%s (%s) (ID:meta - %d:%d)";
        lines.add(String.format(fmt, dname, name, id, meta));

        return lines;
    }

    public static ArrayList<String> getFullBlockInfo(EntityPlayer player, World world, int x, int y, int z)
    {
        ArrayList<String> lines = getBasicBlockInfo(player, world, x, y, z);

        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            te.writeToNBT(nbt);
            lines.add("");
            NBTFormatter.NBTFormatterPretty(lines, nbt);
        }

        return lines;
    }

    public static void printBlockInfoToConsole(EntityPlayer player, World world, int x, int y, int z)
    {
        ArrayList<String> lines = getFullBlockInfo(player, world, x, y, z);

        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void dumpBlockInfoToFile(EntityPlayer player, World world, int x, int y, int z)
    {
        DataDump.dumpDataToFile("block_and_tileentity_data", getFullBlockInfo(player, world, x, y, z));
    }
}
