package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ModNameUtils;

public class ItemDump
{
    private static void addData(DataDump dump, Item item, ResourceLocation rl, boolean hasSubTypes, boolean dumpNBT, @Nonnull ItemStack stack)
    {
        int id = Item.getIdFromItem(item);
        int meta = stack.isEmpty() == false ? stack.getMetadata() : 0;

        String modName = ModNameUtils.getModName(rl);
        String registryName = rl.toString();
        String displayName = stack.isEmpty() == false ? stack.getDisplayName() : DataDump.EMPTY_STRING;

        if (dumpNBT)
        {
            String nbt = stack.isEmpty() == false && stack.getTagCompound() != null ? stack.getTagCompound().toString() : DataDump.EMPTY_STRING;

            dump.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, getOredictKeysJoined(stack), nbt);
        }
        else
        {
            dump.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, getOredictKeysJoined(stack));
        }
    }

    public static List<String> getFormattedItemDump(Format format, boolean dumpNBT)
    {
        DataDump itemDump = new DataDump(dumpNBT ? 8 : 7, format);

        for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            getDataForItemSubtypes(itemDump, entry.getValue(), entry.getKey(), dumpNBT);
        }

        if (dumpNBT)
        {
            itemDump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Ore Dict keys", "NBT");
        }
        else
        {
            itemDump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Ore Dict keys");
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
            for (CreativeTabs tab : item.getCreativeTabs())
            {
                NonNullList<ItemStack> stacks = NonNullList.<ItemStack>create();
                item.getSubItems(tab, stacks);

                for (ItemStack stack : stacks)
                {
                    // FIXME: Ignore identical duplicate entries from different tabs...
                    addData(itemDump, item, rl, true, dumpNBT, stack);
                }
            }
        }
        else
        {
            addData(itemDump, item, rl, false, dumpNBT, new ItemStack(item, 1, 0));
        }
    }

    public static String getOredictKeysJoined(@Nonnull ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return DataDump.EMPTY_STRING;
        }

        int[] ids = OreDictionary.getOreIDs(stack);

        if (ids.length == 0)
        {
            return DataDump.EMPTY_STRING;
        }

        List<String> names = new ArrayList<String>();

        for (int id : ids)
        {
            names.add(OreDictionary.getOreName(id));
        }

        if (names.size() == 1)
        {
            return names.get(0);
        }

        Collections.sort(names);

        return String.join(",", names);
    }

    public static String getStackInfo(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            // old: [%s @ %d - display: %s - NBT: %s]
            int meta = stack.getMetadata();
            String regName = stack.getItem().getRegistryName().toString();

            return String.format("[%s@%d - '%s' - %s]", regName, meta, meta == OreDictionary.WILDCARD_VALUE ? "(WILDCARD)" : stack.getDisplayName(),
                    stack.getTagCompound() != null ? stack.getTagCompound().toString() : "<no NBT>");
        }

        return DataDump.EMPTY_STRING;
    }
}
