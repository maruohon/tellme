package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ModNameUtils;

public class ItemDump
{
    private static void addData(DataDump dump, Item item, ResourceLocation rl, boolean hasSubTypes, boolean dumpNBT, @Nonnull ItemStack stack)
    {
        int id = Item.getIdFromItem(item);
        int meta = stack.isEmpty() == false ? stack.getMetadata() : 0;

        String modName = ModNameUtils.getModName(rl);
        String registryName = rl.toString();
        String displayName = stack.isEmpty() == false ? stack.getDisplayName() : DataDump.EMPTY_STRING;
        displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

        if (dumpNBT)
        {
            String nbt = stack.isEmpty() == false && stack.getTagCompound() != null ? stack.getTagCompound().toString() : DataDump.EMPTY_STRING;

            dump.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, getOredictKeysJoined(stack), nbt);
        }
        else
        {
            dump.addData(modName, registryName, String.valueOf(id), String.valueOf(meta),
                    String.valueOf(hasSubTypes), displayName, getOredictKeysJoined(stack));
        }
    }

    public static List<String> getFormattedItemDump(Format format, boolean dumpNBT)
    {
        DataDump itemDump = new DataDump(dumpNBT ? 8 : 7, format);

        for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            getDataForItemSubtypes(itemDump, entry.getValue(), entry.getKey(), dumpNBT);
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

        itemDump.addHeader("*** WARNING ***");
        itemDump.addHeader("The block and item IDs are dynamic and will be different on each world!");
        itemDump.addHeader("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
        itemDump.addHeader("*** ALSO ***");
        itemDump.addHeader("The server doesn't have a list of sub block and sub items");
        itemDump.addHeader("(= items with different damage value or blocks with different metadata).");
        itemDump.addHeader("That is why the block and item list dumps only contain one entry per block/item class (separate ID) when run on a server.");

        return itemDump.getLines();
    }

    private static void getDataForItemSubtypes(DataDump itemDump, Item item, ResourceLocation rl, boolean dumpNBT)
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
                        addData(itemDump, item, rl, true, dumpNBT, stack);
                    }
                }
            }
        }
        else
        {
            addData(itemDump, item, rl, false, dumpNBT, new ItemStack(item, 1, 0));
        }
    }

    public static String getJsonItemsWithPropsDump()
    {
        HashMultimap<String, ResourceLocation> map = HashMultimap.create(10000, 16);

        // Get a mapping of modName => collection-of-block-names
        for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            ResourceLocation key = entry.getKey();
            map.put(key.getNamespace(), key);
        }

        // First sort by mod name
        List<String> mods = Lists.newArrayList(map.keySet());
        Collections.sort(mods);
        JsonObject root = new JsonObject();

        for (String mod : mods)
        {
            // For each mod, sort the items by their registry name
            List<ResourceLocation> items = Lists.newArrayList(map.get(mod));
            Collections.sort(items);
            JsonObject objectMod = new JsonObject();

            for (ResourceLocation key : items)
            {
                JsonArray arrItem = new JsonArray();
                getDataForItemSubtypesForJson(arrItem, ForgeRegistries.ITEMS.getValue(key), key);
                objectMod.add(key.toString(), arrItem);
            }

            root.add(ModNameUtils.getModName(items.get(0)), objectMod);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(root);
    }

    public static String getOredictKeysJoined(@Nonnull ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return DataDump.EMPTY_STRING;
        }

        int[] ids = OreDictionary.getOreIDs(stack);

        if (ids.length == 0)
        {
            return DataDump.EMPTY_STRING;
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

    public static String getStackInfoBasic(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            // old: [%s @ %d - display: %s - NBT: %s]
            int meta = stack.getMetadata();
            ResourceLocation rl = stack.getItem().getRegistryName();
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = meta == OreDictionary.WILDCARD_VALUE ? "(WILDCARD)" : stack.getDisplayName();
            displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

            return String.format("[%s@%d - '%s']", regName, meta, displayName);
        }

        return DataDump.EMPTY_STRING;
    }

    public static String getStackInfo(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            // old: [%s @ %d - display: %s - NBT: %s]
            int meta = stack.getMetadata();
            ResourceLocation rl = stack.getItem().getRegistryName();
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = meta == OreDictionary.WILDCARD_VALUE ? "(WILDCARD)" : stack.getDisplayName();
            displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

            return String.format("[%s@%d - '%s' - %s]", regName, meta, displayName,
                    stack.getTagCompound() != null ? stack.getTagCompound().toString() : "<no NBT>");
        }

        return DataDump.EMPTY_STRING;
    }

    private static void getDataForItemSubtypesForJson(JsonArray arr, Item item, ResourceLocation rl)
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
                        addDataForItemSubtypeForJson(arr, item, rl, true, stack);
                    }
                }
            }
        }
        else
        {
            addDataForItemSubtypeForJson(arr, item, rl, false, new ItemStack(item, 1, 0));
        }
    }

    private static void addDataForItemSubtypeForJson(JsonArray arr, Item item, ResourceLocation rl, boolean hasSubTypes, ItemStack stack)
    {
        int itemId = Item.getIdFromItem(item);
        int itemMeta = stack.getMetadata();
        String subTypes = hasSubTypes ? String.valueOf(hasSubTypes) : "?";
        String exists = DataDump.isDummied(ForgeRegistries.ITEMS, rl) ? "false" : "true";
        String oreDictKeys = ItemDump.getOredictKeysJoined(stack);
        String itemName = item.getRegistryName().toString();
        String displayName = stack.isEmpty() == false ? stack.getDisplayName() : item.getTranslationKey(stack);
        displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

        JsonObject obj = new JsonObject();
        obj.add("RegistryName", new JsonPrimitive(itemName));
        obj.add("ItemID", new JsonPrimitive(itemId));
        obj.add("ItemMeta", new JsonPrimitive(itemMeta));
        obj.add("SubTypes", new JsonPrimitive(subTypes));
        obj.add("Exists", new JsonPrimitive(exists));
        obj.add("DisplayName", new JsonPrimitive(displayName));

        TellMe.proxy.addCreativeTabNames(obj, item);

        if (item instanceof ItemBlock)
        {
            try
            {
                Block block = ((ItemBlock) item).getBlock();
                String hardness = String.format("%.2f", BlockDump.field_blockHardness.get(block));
                String resistance = String.format("%.2f", BlockDump.field_blockResistance.get(block));

                obj.add("Type", new JsonPrimitive("block"));
                obj.add("Hardness", new JsonPrimitive(hardness));
                obj.add("Resistance", new JsonPrimitive(resistance));
            }
            catch (Exception e) {}
        }
        else if (item instanceof ItemFood)
        {
            ItemFood itemFood = (ItemFood) item;
            String hunger = stack.isEmpty() == false ? String.valueOf(itemFood.getHealAmount(stack)) : "?";
            String saturation = stack.isEmpty() == false ? String.valueOf(itemFood.getSaturationModifier(stack)) : "?";

            obj.add("Type", new JsonPrimitive("food"));
            obj.add("Hunger", new JsonPrimitive(hunger));
            obj.add("Saturation", new JsonPrimitive(saturation));
        }
        else
        {
            obj.add("Type", new JsonPrimitive("generic"));
        }

        obj.add("OreDict", new JsonPrimitive(oreDictKeys));

        arr.add(obj);
    }
}
