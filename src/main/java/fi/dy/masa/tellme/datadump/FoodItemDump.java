package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class FoodItemDump
{
    private static void addData(DataDump dump, Item item, ResourceLocation rl)
    {
        String registryName = rl.toString();
        ItemStack stack = new ItemStack(item);
        String displayName = stack.isEmpty() == false ? stack.getDisplayName().getString() : DataDump.EMPTY_STRING;
        displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

        Food food = item.getFood();
        String hunger = String.valueOf(food.getHealing());
        String saturation = String.valueOf(food.getSaturation());
        String isMeat = String.valueOf(food.isMeat());
        String isFastEat = String.valueOf(food.isFastEating());
        List<Pair<EffectInstance, Float>> effects = food.getEffects();
        String effectsStr = effects.stream()
                .map((pair) -> "{[" + pair.getLeft().toString() + "], Propability: " + pair.getRight() + "}")
                .collect(Collectors.joining(", "));

        dump.addData(registryName, displayName, hunger, saturation, isMeat, isFastEat, ItemDump.getTagNamesJoined(item), effectsStr);
    }

    public static List<String> getFormattedFoodItemDump(Format format)
    {
        DataDump itemDump = new DataDump(8, format);

        for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            Item item = entry.getValue();

            if (item.isFood())
            {
                addData(itemDump, item, entry.getKey());
            }
        }

        itemDump.addTitle("Registry name", "Display name", "Hunger", "Saturation", "Is meat", "Fast to eat", "Tags", "Effects");

        itemDump.setColumnProperties(2, Alignment.RIGHT, true); // hunger
        itemDump.setColumnProperties(3, Alignment.RIGHT, true); // saturation

        return itemDump.getLines();
    }
}
