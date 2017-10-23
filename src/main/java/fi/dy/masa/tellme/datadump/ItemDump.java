package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
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

    private ItemDump(Format format, boolean dumpNBT)
    {
        super(dumpNBT ? 8 : 7, format);

        this.dumpNBT = dumpNBT;
    }

    @Override
    public List<String> getLines()
    {
        if (this.getFormat() != Format.ASCII)
        {
            return super.getLines();
        }

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

    public void addData(Item item, ResourceLocation rl, boolean hasSubTypes, @Nonnull ItemStack stack)
    {
        int id = Item.getIdFromItem(item);
        int meta = stack.isEmpty() == false ? stack.getMetadata() : 0;

        String modName = ModNameUtils.getModName(rl);
        String registryName = rl.toString();
        String displayName = stack.isEmpty() == false ? stack.getDisplayName() : EMPTY_STRING;

        if (this.dumpNBT)
        {
            String nbt = stack.isEmpty() == false && stack.getTagCompound() != null ? stack.getTagCompound().toString() : EMPTY_STRING;

            this.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, getOredictKeysJoined(stack), nbt);
        }
        else
        {
            this.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, getOredictKeysJoined(stack));
        }
    }

    public static List<String> getFormattedItemDump(Format format, boolean dumpNBT)
    {
        ItemDump itemDump = new ItemDump(format, dumpNBT);
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

        itemDump.setColumnProperties(2, Alignment.RIGHT, true); // ID
        itemDump.setColumnProperties(3, Alignment.RIGHT, true); // meta
        itemDump.setColumnAlignment(4, Alignment.RIGHT); // sub-types

        itemDump.setUseColumnSeparator(true);

        return itemDump.getLines();
    }

    public static String getOredictKeysJoined(@Nonnull ItemStack stack)
    {
        if (stack.isEmpty())
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

        return EMPTY_STRING;
    }
}
