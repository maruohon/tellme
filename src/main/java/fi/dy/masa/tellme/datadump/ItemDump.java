package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import fi.dy.masa.tellme.util.ModNameUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class ItemDump extends DataDump
{
    private boolean dumpNBT;

    private ItemDump(Format format, boolean dumpNBT)
    {
        super(dumpNBT ? 7 : 6, format);

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
                    String.valueOf(hasSubTypes), displayName, nbt);
        }
        else
        {
            this.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName);
        }
    }

    public static List<String> getFormattedItemDump(Format format, boolean dumpNBT)
    {
        ItemDump itemDump = new ItemDump(format, dumpNBT);

        for (ResourceLocation key : Item.REGISTRY.getKeys())
        {
            getDataForItemSubtypes(Item.REGISTRY.getObject(key), key, itemDump);
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

        return itemDump.getLines();
    }

    public static void getDataForItemSubtypes(Item item, ResourceLocation rl, ItemDump itemDump)
    {
        if (item.getHasSubtypes())
        {
            NonNullList<ItemStack> stacks = NonNullList.<ItemStack>create();
            CreativeTabs tab = item.getCreativeTab();
            item.getSubItems(tab, stacks);

            for (ItemStack stack : stacks)
            {
                // FIXME: Ignore identical duplicate entries from different tabs...
                itemDump.addData(item, rl, true, stack);
            }
        }
        else
        {
            itemDump.addData(item, rl, false, new ItemStack(item, 1, 0));
        }
    }

    public static String getStackInfo(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            // old: [%s @ %d - display: %s - NBT: %s]
            int meta = stack.getMetadata();
            String regName = Item.REGISTRY.getNameForObject(stack.getItem()).toString();

            return String.format("[%s@%d - '%s' - %s]", regName, meta, meta == 32767 ? "(WILDCARD)" : stack.getDisplayName(),
                    stack.getTagCompound() != null ? stack.getTagCompound().toString() : "<no NBT>");
        }

        return EMPTY_STRING;
    }
}
