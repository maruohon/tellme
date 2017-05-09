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
    private OreDictionaryDump(int columns, Format format)
    {
        super(columns, format);
    }

    public static List<String> getFormattedOreDictionaryDump(Format format, boolean byItemStack)
    {
        OreDictionaryDump oreDictDump;
        String[] oreNames = OreDictionary.getOreNames();

        if (byItemStack)
        {
            oreDictDump = new OreDictionaryDump(5, format);
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
                int meta = stack.getMetadata();
                String regName = stack.getItem().getRegistryName().toString();
                String strNBT = stack.hasTagCompound() ? stack.getTagCompound().toString() : "-";

                if (meta == OreDictionary.WILDCARD_VALUE)
                {
                    oreDictDump.addData(regName, String.format("(WILDCARD) %5d", stack.getMetadata()),
                            "-", ItemDump.getOredictKeysJoined(stack), strNBT);
                }
                else
                {
                    oreDictDump.addData(stack.getItem().getRegistryName().toString(), String.format("%5d", stack.getMetadata()),
                            stack.getDisplayName(), ItemDump.getOredictKeysJoined(stack), strNBT);
                }
            }

            oreDictDump.addTitle("Registry name", "Meta/dmg", "Display name", "Ore Dict Keys", "NBT");
            oreDictDump.setColumnAlignment(1, Alignment.RIGHT);
            oreDictDump.setUseColumnSeparator(true);
        }
        else
        {
            oreDictDump = new OreDictionaryDump(2, format);
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
