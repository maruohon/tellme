package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import fi.dy.masa.malilib.util.ItemUtils;
import fi.dy.masa.tellme.LiteModTellMe;
import fi.dy.masa.tellme.command.SubCommand;
import fi.dy.masa.tellme.datadump.DataDump;

public class ItemInfo
{
    public static boolean areItemStacksEqual(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2)
    {
        boolean isEmpty1 = ItemUtils.isEmpty(stack1);
        boolean isEmpty2 = ItemUtils.isEmpty(stack2);

        if (isEmpty1 || isEmpty2)
        {
            return isEmpty1 == isEmpty2;
        }

        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

    private static List<String> getFullItemInfo(@Nonnull ItemStack stack)
    {
        List<String> lines = new ArrayList<>();
        NBTTagCompound tag = ItemUtils.getTag(stack);

        lines.add(ItemData.getFor(stack).toString());

        if (tag == null)
        {
            return lines;
        }

        lines.add("");
        lines.add(tag.toString());
        lines.add("");
        NBTFormatter.getPrettyFormattedNBT(lines, tag);

        return lines;
    }

    public static void printBasicItemInfoToChat(EntityPlayer player, @Nonnull ItemStack stack)
    {
        player.sendMessage(ItemData.getFor(stack).toChatMessage());
    }

    public static void printItemInfoToConsole(@Nonnull ItemStack stack)
    {
        List<String> lines = getFullItemInfo(stack);

        for (String line : lines)
        {
            LiteModTellMe.logger.info(line);
        }
    }

    public static void dumpItemInfoToFile(EntityPlayer player, @Nonnull ItemStack stack)
    {
        File file = DataDump.dumpDataToFile("item_data", getFullItemInfo(stack));
        SubCommand.sendClickableLinkMessage(player, "Output written to file %s", file);
    }

    public static void printItemInfo(EntityPlayer player, @Nonnull ItemStack stack, boolean dumpToFile)
    {
        printBasicItemInfoToChat(player, stack);

        if (dumpToFile)
        {
            dumpItemInfoToFile(player, stack);
        }
        else
        {
            printItemInfoToConsole(stack);
        }
    }

    public static class ItemData
    {
        private final String regName;
        private final int id;
        private final int meta;
        private final String displayName;
        private final String nbtInfo;

        public ItemData(String displayName, String regName, int id, int meta, String nbtInfo)
        {
            this.displayName = displayName;
            this.regName = regName;
            this.id = id;
            this.meta = meta;
            this.nbtInfo = nbtInfo;
        }

        public static ItemData getFor(ItemStack stack)
        {
            String registryName = Item.REGISTRY.getNameForObject(stack.getItem()).toString();
            String nbtInfo;

            if (stack.hasTagCompound())
            {
                nbtInfo = "has NBT data";
            }
            else
            {
                nbtInfo = "no NBT data";
            }

            return new ItemData(stack.getDisplayName(), registryName, Item.getIdFromItem(stack.getItem()), stack.getMetadata(), nbtInfo);
        }

        public ITextComponent toChatMessage()
        {
            String copyStr = this.meta != 0 ? this.regName + ":" + this.meta : this.regName;
            String textPre = String.format("%s (", this.displayName);
            String textPost = String.format(" - %d:%d) %s", this.id, this.meta, this.nbtInfo);

            return ChatUtils.getClipboardCopiableMessage(textPre, copyStr, textPost);
        }

        @Override
        public String toString()
        {
            return String.format("%s (%s - %d:%d) %s", this.displayName, this.regName, this.id, this.meta, this.nbtInfo);
        }
    }
}
