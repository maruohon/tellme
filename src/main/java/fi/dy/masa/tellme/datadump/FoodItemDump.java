package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ModNameUtils;

public class FoodItemDump
{
    private static void addData(DataDump dump, ItemFood item, ResourceLocation rl, boolean hasSubTypes, @Nonnull ItemStack stack)
    {
        int id = Item.getIdFromItem(item);
        int meta = stack.isEmpty() == false ? stack.getMetadata() : 0;

        String modName = ModNameUtils.getModName(rl);
        String registryName = rl.toString();
        String displayName = stack.isEmpty() == false ? stack.getDisplayName() : DataDump.EMPTY_STRING;
        displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);
        String hunger = stack.isEmpty() == false ? String.valueOf(item.getHealAmount(stack)) : "?";
        String saturation = stack.isEmpty() == false ? String.valueOf(item.getSaturationModifier(stack)) : "?";

        dump.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                String.valueOf(hasSubTypes), displayName, hunger, saturation, ItemDump.getOredictKeysJoined(stack));
    }

    public static List<String> getFormattedFoodItemDump(Format format)
    {
        DataDump itemDump = new DataDump(9, format);

        for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            Item item = entry.getValue();

            if (item instanceof ItemFood)
            {
                getDataForItemSubtypes(itemDump, (ItemFood) item, entry.getKey());
            }
        }

        itemDump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Hunger", "Saturation", "Ore Dict keys");

        itemDump.setColumnProperties(2, Alignment.RIGHT, true); // ID
        itemDump.setColumnProperties(3, Alignment.RIGHT, true); // meta
        itemDump.setColumnAlignment(4, Alignment.RIGHT); // sub-types
        itemDump.setColumnProperties(6, Alignment.RIGHT, true); // hunger
        itemDump.setColumnProperties(7, Alignment.RIGHT, true); // saturation

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

    public static void getDataForItemSubtypes(DataDump itemDump, ItemFood item, ResourceLocation rl)
    {
        if (item.getHasSubtypes())
        {
            for (CreativeTabs tab : item.getCreativeTabs())
            {
                if (tab != null)
                {
                    NonNullList<ItemStack> stacks = NonNullList.<ItemStack>create();
                    item.getSubItems(tab, stacks);

                    for (ItemStack stack : stacks)
                    {
                        // FIXME: Ignore identical duplicate entries from different tabs...
                        addData(itemDump, item, rl, true, stack);
                    }
                }
            }
        }
        else
        {
            addData(itemDump, item, rl, false, new ItemStack(item, 1, 0));
        }
    }
}
