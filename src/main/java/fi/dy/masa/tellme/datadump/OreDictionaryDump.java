package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public static List<String> getFormattedOreDictionaryDump(Format format, OreDumpType type)
    {
        OreDictionaryDump oreDictDump;
        List<String> oreNames = Arrays.asList(OreDictionary.getOreNames());

        if (type == OreDumpType.BY_STACK)
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
                String oreDictKeys = ItemDump.getOredictKeysJoined(stack);
                String strNBT = stack.hasTagCompound() ? stack.getTagCompound().toString() : EMPTY_STRING;

                if (meta == OreDictionary.WILDCARD_VALUE)
                {
                    oreDictDump.addData(regName, String.valueOf(meta), "(WILDCARD)", oreDictKeys, strNBT);
                }
                else
                {
                    oreDictDump.addData(regName, String.valueOf(meta), stack.getDisplayName(), oreDictKeys, strNBT);
                }
            }

            oreDictDump.addTitle("Registry name", "Meta/dmg", "Display name", "Ore Dict Keys", "NBT");

            oreDictDump.setColumnProperties(1, Alignment.RIGHT, true);

            oreDictDump.setUseColumnSeparator(true);

            return oreDictDump.getLines();
        }
        else if (type == OreDumpType.BY_ORE_GROUPED)
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

                oreDictDump.addData(name, String.join("; ", stackStrs));
            }

            oreDictDump.addTitle("OreDict Key", "ItemStacks");
            oreDictDump.setUseColumnSeparator(true);

            return oreDictDump.getLines();
        }
        else if (type == OreDumpType.BY_ORE_INDIVIDUAL)
        {
            oreDictDump = new OreDictionaryDump(5, format);

            for (String oreName : oreNames)
            {
                List<ItemStack> stacks = OreDictionary.getOres(oreName);

                for (ItemStack stack : stacks)
                {
                    int meta = stack.getMetadata();
                    String regName = stack.getItem().getRegistryName().toString();
                    String strNBT = stack.hasTagCompound() ? stack.getTagCompound().toString() : EMPTY_STRING;

                    if (meta == OreDictionary.WILDCARD_VALUE)
                    {
                        oreDictDump.addData(oreName, regName, String.valueOf(meta), "(WILDCARD)", strNBT);
                    }
                    else
                    {
                        oreDictDump.addData(oreName, regName, String.valueOf(meta), stack.getDisplayName(), strNBT);
                    }
                }
            }

            oreDictDump.addTitle("OreDict Key", "Registry name", "Meta/dmg", "Display name", "NBT");

            oreDictDump.setColumnProperties(2, Alignment.RIGHT, true);

            oreDictDump.setUseColumnSeparator(true);

            return oreDictDump.getLines();
        }

        return Collections.emptyList();
    }

    public enum OreDumpType
    {
        BY_STACK,
        BY_ORE_GROUPED,
        BY_ORE_INDIVIDUAL
    }
}
