package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import fi.dy.masa.malilib.util.ItemUtils;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ModNameUtils;

public class ItemDump
{
    private static void addData(DataDump dump, Item item, ResourceLocation rl, boolean hasSubTypes, boolean dumpNBT, @Nonnull ItemStack stack)
    {
        boolean notEmpty = ItemUtils.notEmpty(stack);
        int id = Item.getIdFromItem(item);
        int meta = notEmpty ? stack.getMetadata() : 0;

        String modName = ModNameUtils.getModName(rl);
        String registryName = rl.toString();
        String displayName = notEmpty ? stack.getDisplayName() : DataDump.EMPTY_STRING;
        displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

        if (dumpNBT)
        {
            NBTTagCompound tag = ItemUtils.getTag(stack);
            String nbt = notEmpty && tag != null ? tag.toString() : DataDump.EMPTY_STRING;

            dump.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, nbt);
        }
        else
        {
            dump.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName);
        }
    }

    public static List<String> getFormattedItemDump(Format format, boolean dumpNBT)
    {
        DataDump itemDump = new DataDump(dumpNBT ? 7 : 6, format);

        for (ResourceLocation key : Item.REGISTRY.getKeys())
        {
            getDataForItemSubtypes(itemDump, Item.REGISTRY.getObject(key), key, dumpNBT);
        }

        if (dumpNBT)
        {
            itemDump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "NBT");
        }
        else
        {
            itemDump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name");
        }

        itemDump.setColumnProperties(2, Alignment.RIGHT, true); // ID
        itemDump.setColumnProperties(3, Alignment.RIGHT, true); // meta
        itemDump.setColumnAlignment(4, Alignment.RIGHT); // sub-types

        itemDump.setUseColumnSeparator(true);

        itemDump.addHeader("*** WARNING ***");
        itemDump.addHeader("The block and item IDs are dynamic and will be different on each world!");
        itemDump.addHeader("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
        itemDump.addHeader("*** ALSO ***");
        itemDump.addHeader("The server doesn't have a list of sub block and sub items");
        itemDump.addHeader("(= items with different damage value or blocks with different metadata).");
        itemDump.addHeader("That is why the block and item list dumps only contain one entry per block/item class (separate ID) when run on a server.");

        return itemDump.getLines();
    }

    public static void getDataForItemSubtypes(DataDump itemDump, Item item, ResourceLocation rl, boolean dumpNBT)
    {
        if (item.getHasSubtypes())
        {
            NonNullList<ItemStack> stacks = NonNullList.create();
            CreativeTabs tab = item.getCreativeTab();

            if (tab != null)
            {
                item.getSubItems(tab, stacks);

                for (ItemStack stack : stacks)
                {
                    addData(itemDump, item, rl, true, dumpNBT, stack);
                }
            }
        }
        else
        {
            addData(itemDump, item, rl, false, dumpNBT, new ItemStack(item, 1, 0));
        }
    }

    public static String getStackInfoBasic(ItemStack stack)
    {
        if (ItemUtils.notEmpty(stack))
        {
            // old: [%s @ %d - display: %s - NBT: %s]
            int meta = stack.getMetadata();
            ResourceLocation rl = Item.REGISTRY.getNameForObject(stack.getItem());
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = meta == 32767 ? "(WILDCARD)" : stack.getDisplayName();
            displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

            return String.format("[%s@%d - '%s']", regName, meta, displayName);
        }

        return DataDump.EMPTY_STRING;
    }

    public static String getStackInfo(ItemStack stack)
    {
        if (ItemUtils.notEmpty(stack))
        {
            // old: [%s @ %d - display: %s - NBT: %s]
            int meta = stack.getMetadata();

            ResourceLocation rl = Item.REGISTRY.getNameForObject(stack.getItem());
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = meta == 32767 ? "(WILDCARD)" : stack.getDisplayName();
            displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);
            NBTTagCompound tag = ItemUtils.getTag(stack);

            return String.format("[%s@%d - '%s' - %s]", regName, meta, displayName,
                    tag != null ? tag.toString() : "<no NBT>");
        }

        return DataDump.EMPTY_STRING;
    }
}
