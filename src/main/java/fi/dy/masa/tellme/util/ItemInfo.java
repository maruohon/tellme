package fi.dy.masa.tellme.util;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import fi.dy.masa.tellme.TellMe;

public class ItemInfo
{
    public static ArrayList<String> getBasicItemInfo(EntityPlayer player, int slot)
    {
        ArrayList<String> lines = new ArrayList<String>();

        ItemStack stack = player.inventory.getStackInSlot(slot);

        if (stack == null || stack.getItem() == null)
        {
            return lines;
        }

        String name = Item.itemRegistry.getNameForObject(stack.getItem()).toString();
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

    public static ArrayList<String> getFullItemInfo(EntityPlayer player, int slot)
    {
        ArrayList<String> lines = getBasicItemInfo(player, slot);

        ItemStack stack = player.inventory.getStackInSlot(slot);

        if (stack == null || stack.getItem() == null || stack.hasTagCompound() == false)
        {
            return lines;
        }

        lines.add("");
        NBTFormatter.NBTFormatterPretty(lines, stack.getTagCompound());

        return lines;
    }

    public static void printBasicItemInfoToChat(EntityPlayer player, int slot)
    {
        for (String line : getBasicItemInfo(player, slot))
        {
            player.addChatMessage(new ChatComponentText(line));
        }
    }

    public static void printItemInfoToConsole(EntityPlayer player, int slot)
    {
        ArrayList<String> lines = getFullItemInfo(player, slot);

        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void dumpItemInfoToFile(EntityPlayer player, int slot)
    {
        DataDump.dumpDataToFile("item_data", getFullItemInfo(player, slot));
    }
}
