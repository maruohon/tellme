package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.ModNameUtils;

public class ItemDump extends DataDump
{
    private boolean dumpNBT;

    private ItemDump(boolean dumpNBT)
    {
        super(dumpNBT ? 8 : 7);

        this.dumpNBT = dumpNBT;
    }

    protected List<String> getLines()
    {
        List<String> lines = new ArrayList<String>();

        this.generateFormatStrings();

        lines.add(this.lineSeparator);
        lines.add("*** WARNING ***");
        lines.add("The block and item IDs are dynamic and will be different on each world!");
        lines.add("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
        lines.add("*** ALSO ***");
        lines.add("The server doesn't have a list of sub block and sub items");
        lines.add("(= items with different damage value or blocks with different metadata).");
        lines.add("That is why the block and item list dumps only contain one entry per block/item class (separate ID) when run on a server.");

        // Get the actual data
        this.getFormattedData(lines);

        return lines;
    }

    public void addData(Item item, ResourceLocation rl, boolean hasSubTypes, @Nullable ItemStack stack)
    {
        int id = Item.getIdFromItem(item);
        int meta = stack != null ? stack.getMetadata() : 0;

        String modName = ModNameUtils.getModName(rl);
        String registryName = rl.toString();
        String displayName = stack != null ? stack.getDisplayName() : EMPTY_STRING;

        if (this.dumpNBT)
        {
            String nbt = stack != null && stack.getTagCompound() != null ? stack.getTagCompound().toString() : "-";

            this.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, getOredictKeysJoined(stack), nbt);
        }
        else
        {
            this.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, getOredictKeysJoined(stack));
        }
    }

    public static List<String> getFormattedItemDump(boolean dumpNBT)
    {
        ItemDump itemDump = new ItemDump(dumpNBT);
        Iterator<Map.Entry<ResourceLocation, Item>> iter = ForgeRegistries.ITEMS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Item> entry = iter.next();
            TellMe.proxy.getDataForItemSubtypes(entry.getValue(), entry.getKey(), itemDump);
        }

        if (dumpNBT)
        {
            itemDump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Ore Dict keys", "NBT");
        }
        else
        {
            itemDump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Ore Dict keys");
        }

        itemDump.setColumnAlignment(2, Alignment.RIGHT); // ID
        itemDump.setColumnAlignment(3, Alignment.RIGHT); // meta
        itemDump.setColumnAlignment(4, Alignment.RIGHT); // sub-types
        itemDump.setUseColumnSeparator(true);

        return itemDump.getLines();
    }

    public static String getOredictKeysJoined(@Nullable ItemStack stack)
    {
        if (stack == null)
        {
            return EMPTY_STRING;
        }

        int[] ids = OreDictionary.getOreIDs(stack);

        if (ids.length == 0)
        {
            return EMPTY_STRING;
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

        return String.join(", ", names);
    }

    public static String getStackInfo(ItemStack stack)
    {
        if (isStackEmpty(stack) == false)
        {
            // old: [%s @ %d - display: %s - NBT: %s]
            return String.format("[%s@%d - '%s' - %s]",
                    stack.getItem().getRegistryName(), stack.getMetadata(), stack.getDisplayName(),
                    stack.getTagCompound() != null ? stack.getTagCompound().toString() : "<no NBT>");
        }

        return "";
    }

    public static boolean isStackEmpty(ItemStack stack)
    {
        return stack == null;
    }
}
