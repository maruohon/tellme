package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.tellme.util.ItemType;

public class OreDictionaryDump extends DataDump
{
    private OreDictionaryDump(int columns)
    {
        super(columns);
    }

    public static List<String> getFormattedOreDictionaryDump(boolean byItemStack)
    {
        OreDictionaryDump oreDictDump;
        String[] oreNames = OreDictionary.getOreNames();

        if (byItemStack)
        {
            oreDictDump = new OreDictionaryDump(5);
            Set<ItemType> allStacks = new HashSet<ItemType>();

            for (String name : oreNames)
            {
                List<ItemStack> stacks = OreDictionary.getOres(name);

                for (ItemStack stack : stacks)
                {
                    allStacks.add(new ItemType(stack));
                }
            }

            Iterator<ItemType> iter = allStacks.iterator();

            while (iter.hasNext())
            {
                ItemStack stack = iter.next().getStack();
                String strMeta = stack.getMetadata() == OreDictionary.WILDCARD_VALUE ?
                        String.format("(WILDCARD) %5d", stack.getMetadata()) : String.format("%5d", stack.getMetadata());
                String strNBT = stack.hasTagCompound() ? stack.getTagCompound().toString() : "-";
                oreDictDump.addData(stack.getItem().getRegistryName().toString(), strMeta,
                        stack.getDisplayName(), ItemDump.getOredictKeysJoined(stack), strNBT);
            }

            oreDictDump.addTitle("Registry name", "Meta/dmg", "Display name", "Ore Dict Keys", "NBT");
            oreDictDump.setColumnAlignment(1, Alignment.RIGHT);
            oreDictDump.setUseColumnSeparator(true);
        }
        else
        {
            oreDictDump = new OreDictionaryDump(2);
            for (String name : oreNames)
            {
                List<ItemStack> stacks = OreDictionary.getOres(name);
                List<String> stackStrs = new ArrayList<String>();

                for (ItemStack stack : stacks)
                {
                    stackStrs.add(ItemDump.getStackInfo(stack));
                }

                oreDictDump.addData(name, String.join(", ", stackStrs));
            }

            oreDictDump.addTitle("Key", "ItemStacks");
            oreDictDump.setUseColumnSeparator(true);
        }

        return oreDictDump.getLines();
    }
}
