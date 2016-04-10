package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

import fi.dy.masa.tellme.TellMe;

public class ItemInfo
{
    private static List<String> getBasicItemInfo(ItemStack stack)
    {
        List<String> lines = new ArrayList<String>();
        String name = Item.REGISTRY.getNameForObject(stack.getItem()).toString();
        String dname = stack.getDisplayName();
        String nbtInfo;

        if (stack.hasTagCompound() == true)
        {
            nbtInfo = "has NBT data";
        }
        else
        {
            nbtInfo = "no NBT data";
        }

        String fmt = "%s (%s - %d:%d) %s";
        lines.add(String.format(fmt, dname, name, Item.getIdFromItem(stack.getItem()), stack.getItemDamage(), nbtInfo));

        return lines;
    }

    private static List<String> getFullItemInfo(ItemStack stack)
    {
        List<String> lines = getBasicItemInfo(stack);
        if (stack.hasTagCompound() == false)
        {
            return lines;
        }

        lines.add("");
        NBTFormatter.getPrettyFormattedNBT(lines, stack.getTagCompound());

        return lines;
    }

    public static void printBasicItemInfoToChat(EntityPlayer player, ItemStack stack)
    {
        for (String line : getBasicItemInfo(stack))
        {
            player.addChatMessage(new TextComponentString(line));
        }
    }

    public static void printItemInfoToConsole(ItemStack stack)
    {
        List<String> lines = getFullItemInfo(stack);

        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void dumpItemInfoToFile(EntityPlayer player, ItemStack stack)
    {
        File f = DataDump.dumpDataToFile("item_data", getFullItemInfo(stack));
        player.addChatMessage(new TextComponentString("Output written to file " + f.getName()));
    }
}
