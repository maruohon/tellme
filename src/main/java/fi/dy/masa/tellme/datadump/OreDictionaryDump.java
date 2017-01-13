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
    private OreDictionaryDump()
    {
        super(2);
    }

    public static List<String> getFormattedOreDictionaryDump(boolean byItemStack)
    {
        OreDictionaryDump oreDictDump = new OreDictionaryDump();
        String[] oreNames = OreDictionary.getOreNames();

        if (byItemStack)
        {
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
                oreDictDump.addData(ItemDump.getStackInfo(stack), ItemDump.getOredictKeysJoined(stack));
            }

            oreDictDump.addTitle("ItemStack", "Keys");
            oreDictDump.setUseColumnSeparator(true);
        }
        else
        {
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
