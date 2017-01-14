package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;

public class ItemInfo
{
    public static boolean areItemStacksEqual(@Nullable ItemStack stack1, @Nullable ItemStack stack2)
    {
        if (stack1.isEmpty() || stack2.isEmpty())
        {
            return stack1.isEmpty() == stack2.isEmpty();
        }

        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    private static List<String> getBasicItemInfo(ItemStack stack)
    {
        List<String> lines = new ArrayList<String>();
        String name = Item.REGISTRY.getNameForObject(stack.getItem()).toString();
        String dname = stack.getDisplayName();
        String nbtInfo;

        if (stack.hasTagCompound())
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
        lines.add(stack.getTagCompound().toString());
        lines.add("");
        NBTFormatter.getPrettyFormattedNBT(lines, stack.getTagCompound());

        return lines;
    }

    public static void printBasicItemInfoToChat(EntityPlayer player, ItemStack stack)
    {
        for (String line : getBasicItemInfo(stack))
        {
            player.sendMessage(new TextComponentString(line));
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
        player.sendMessage(new TextComponentString("Output written to file " + f.getName()));
    }

    public static void printItemInfo(EntityPlayer player, @Nonnull ItemStack stack, boolean dumpToFile)
    {
        ItemInfo.printBasicItemInfoToChat(player, stack);

        if (dumpToFile)
        {
            ItemInfo.dumpItemInfoToFile(player, stack);
        }
        else
        {
            ItemInfo.printItemInfoToConsole(stack);
        }
    }
}
