package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class FoodItemDump
{
    private static void addData(DataDump dump, Item item, Identifier rl, ItemDump.ItemDumpContext ctx)
    {
        String registryName = rl.toString();
        ItemStack stack = new ItemStack(item);
        String displayName = stack.isEmpty() == false ? stack.getName().getString() : DataDump.EMPTY_STRING;
        displayName = Formatting.strip(displayName);

        FoodComponent food = item.getFoodComponent();
        String hunger = String.valueOf(food.getHunger());
        String saturation = String.valueOf(food.getSaturationModifier());
        String isMeat = String.valueOf(food.isMeat());
        String isFastEat = String.valueOf(food.isAlwaysEdible());
        List<Pair<StatusEffectInstance, Float>> effects = food.getStatusEffects();
        String effectsStr = effects.stream()
                .map((pair) -> "{[" + pair.getLeft().toString() + "], Propability: " + pair.getRight() + "}")
                .collect(Collectors.joining(", "));

        dump.addData(registryName, displayName, hunger, saturation, isMeat, isFastEat, ItemDump.getTagNamesJoined(item, ctx.tagMap), effectsStr);
    }

    public static List<String> getFormattedFoodItemDump(Format format)
    {
        DataDump itemDump = new DataDump(8, format);
        ItemDump.ItemDumpContext ctx = new ItemDump.ItemDumpContext(ItemDump.createItemTagMap());

        for (Identifier id : Registry.ITEM.getIds())
        {
            Item item = Registry.ITEM.get(id);

            if (item.isFood())
            {
                addData(itemDump, item, id, ctx);
            }
        }

        itemDump.addTitle("Registry name", "Display name", "Hunger", "Saturation", "Is meat", "Fast to eat", "Tags", "Effects");

        itemDump.setColumnProperties(2, Alignment.RIGHT, true); // hunger
        itemDump.setColumnProperties(3, Alignment.RIGHT, true); // saturation

        return itemDump.getLines();
    }
}
