package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ModNameUtils;

public class ItemDump
{
    public static final String[] HARVEST_LEVEL_NAMES = new String[] { "Wood/Gold", "Stone", "Iron", "Diamond" };

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

    public static String getJsonItemsWithPropsDump(EntityPlayer player)
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
                getDataForItemSubtypesForJson(arrItem, ForgeRegistries.ITEMS.getValue(key), key, player);
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

    private static void getDataForItemSubtypesForJson(JsonArray arr, Item item, ResourceLocation rl, EntityPlayer player)
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
                        addDataForItemSubtypeForJson(arr, item, rl, true, stack, player);
                    }
                }
            }
        }
        else
        {
            addDataForItemSubtypeForJson(arr, item, rl, false, new ItemStack(item, 1, 0), player);
        }
    }

    private static void addDataForItemSubtypeForJson(JsonArray arr, Item item, ResourceLocation rl, boolean hasSubTypes, ItemStack stack, EntityPlayer player)
    {
        int itemId = Item.getIdFromItem(item);
        int itemMeta = stack.getMetadata();
        int maxDamage = stack.getMaxDamage();
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
        obj.add("MaxStackSize", new JsonPrimitive(stack.getMaxStackSize()));

        if (maxDamage > 0)
        {
            obj.add("MaxDurability", new JsonPrimitive(maxDamage));
        }

        obj.add("SubTypes", new JsonPrimitive(subTypes));
        obj.add("Exists", new JsonPrimitive(exists));
        obj.add("DisplayName", new JsonPrimitive(displayName));

        TellMe.proxy.addCreativeTabNames(obj, item);

        if (item instanceof ItemBlock)
        {
            try
            {
                World world = player.getEntityWorld();
                BlockPos pos = BlockPos.ORIGIN;
                ItemStack stackBefore = player.getHeldItemMainhand();
                Block block = ((ItemBlock) item).getBlock();
                IBlockState state = block.getDefaultState();

                try {
                    setHeldItemWithoutEquipSound(player, EnumHand.MAIN_HAND, stack);
                    state = block.getStateForPlacement(world, pos, EnumFacing.UP, 0.5f, 1f, 0.5f, itemMeta, player, EnumHand.MAIN_HAND);
                } catch (Exception e) {}

                setHeldItemWithoutEquipSound(player, EnumHand.MAIN_HAND, stackBefore);

                String hardness = String.format("%.2f", BlockDump.field_blockHardness.get(block));
                String resistance = String.format("%.2f", BlockDump.field_blockResistance.get(block));
                String tool = block.getHarvestTool(state);
                int harvestLevel = block.getHarvestLevel(state);
                String harvestLevelName = (harvestLevel >= 0 && harvestLevel < HARVEST_LEVEL_NAMES.length) ? HARVEST_LEVEL_NAMES[harvestLevel] : "Unknown";
                boolean fallingBlock = block instanceof BlockFalling;

                @SuppressWarnings("deprecation")
                int light = state.getLightValue();
                // Ugly way to try to get the flammability...
                boolean flammable = block.getFlammability(world, pos, EnumFacing.UP) > 0;//Blocks.FIRE.getFlammability(block) > 0;
                @SuppressWarnings("deprecation")
                int opacity = state.getLightOpacity();

                obj.add("Type", new JsonPrimitive("block"));
                obj.add("Hardness", new JsonPrimitive(hardness));
                obj.add("Resistance", new JsonPrimitive(resistance));
                obj.add("LightValue", new JsonPrimitive(light));
                obj.add("LightOpacity", new JsonPrimitive(opacity));
                obj.add("Flammable", new JsonPrimitive(flammable));
                obj.add("HarvestTool", new JsonPrimitive(tool));
                obj.add("HarvestLevel", new JsonPrimitive(harvestLevel));
                obj.add("HarvestLevelName", new JsonPrimitive(harvestLevelName));
                obj.add("FallingBlock", new JsonPrimitive(fallingBlock));
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
            List<String> toolClasses = new ArrayList<>(item.getToolClasses(stack));

            if (toolClasses.isEmpty() == false)
            {
                StringBuilder levels = new StringBuilder(32);
                levels.append(String.valueOf(item.getHarvestLevel(stack, toolClasses.get(0), null, null)));

                for (int i = 1; i < toolClasses.size(); ++i)
                {
                    levels.append(",").append(item.getHarvestLevel(stack, toolClasses.get(i), null, null));
                }

                obj.add("ToolClasses", new JsonPrimitive(String.join(",", toolClasses)));
                obj.add("HarvestLevels", new JsonPrimitive(levels.toString()));

                if (item instanceof ItemTool)
                {
                    obj.add("ToolMaterial", new JsonPrimitive(((ItemTool) item).getToolMaterialName()));
                }
            }

            Multimap<String, AttributeModifier> attributes = item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND, stack);

            if (attributes.isEmpty() == false)
            {
                JsonArray attributeArr = new JsonArray();

                for (Entry<String, AttributeModifier> entry : attributes.entries())
                {
                    JsonObject o1 = new JsonObject();
                    JsonObject o2 = new JsonObject();
                    o1.add("Type", new JsonPrimitive(entry.getKey()));
                    o1.add("Value", o2);

                    AttributeModifier att = entry.getValue();
                    o2.add("Name", new JsonPrimitive(att.getName()));
                    o2.add("Operation", new JsonPrimitive(att.getOperation()));
                    o2.add("Amount", new JsonPrimitive(att.getAmount()));

                    attributeArr.add(o1);
                }

                obj.add("Attributes", attributeArr);
            }
        }

        obj.add("OreDict", new JsonPrimitive(oreDictKeys));

        arr.add(obj);
    }

    public static void setHeldItemWithoutEquipSound(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (hand == EnumHand.MAIN_HAND)
        {
            player.inventory.mainInventory.set(player.inventory.currentItem, stack);
        }
        else if (hand == EnumHand.OFF_HAND)
        {
            player.inventory.offHandInventory.set(0, stack);
        }
    }
}
