package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ItemType;
import fi.dy.masa.tellme.util.ModNameUtils;

public class ItemDump
{
    public static final String[] HARVEST_LEVEL_NAMES = new String[] { "Wood/Gold", "Stone", "Iron", "Diamond" };

    public static final ItemInfoProviderBase INFO_BASIC = new ItemInfoProviderBasic();
    public static final ItemInfoProviderBase INFO_NBT = new ItemInfoProviderNBT();
    public static final ItemInfoProviderBase INFO_PLANTABLES = new ItemInfoProviderPlantables();
    public static final ItemInfoProviderBase INFO_TOOL_CLASS = new ItemInfoProviderToolClasses();
    public static final ItemInfoProviderBase INFO_CRAFTABLES = new ItemInfoProviderCraftables();

    public static List<String> getFormattedItemDump(Format format, ItemInfoProviderBase provider)
    {
        DataDump itemDump = new DataDump(provider.getColumnCount(), format);

        for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            getDataForItemSubtypes(itemDump, entry.getValue(), entry.getKey(), provider);
        }

        provider.addTitle(itemDump);
        provider.addHeaders(itemDump);

        return itemDump.getLines();
    }

    public static List<String> getFormattedCraftableItemsDump(Format format)
    {
        ItemInfoProviderBase provider = INFO_CRAFTABLES;
        DataDump dump = new DataDump(provider.getColumnCount(), format);

        for (Map.Entry<ResourceLocation, IRecipe> entry : ForgeRegistries.RECIPES.getEntries())
        {
            IRecipe recipe = entry.getValue();
            ItemStack stack = recipe.getRecipeOutput();

            if (recipe.canFit(3, 3) && stack.isEmpty() == false)
            {
                provider.addLine(dump, stack, entry.getKey());
            }
        }

        provider.addTitle(dump);
        provider.addHeaders(dump);

        return dump.getLines();
    }

    private static void getDataForItemSubtypes(DataDump itemDump, Item item, ResourceLocation rl, ItemInfoProviderBase provider)
    {
        CreativeTabs[] tabs = item.getCreativeTabs();

        if (item.getHasSubtypes())
        {
            if (tabs == null || tabs.length == 0 || tabs[0] == null)
            {
                tabs = CreativeTabs.CREATIVE_TAB_ARRAY;
            }

            int count = 0;
            HashSet<ItemType> addedItems = new HashSet<>();

            for (CreativeTabs tab : tabs)
            {
                if (tab != null)
                {
                    NonNullList<ItemStack> stacks = NonNullList.create();
                    item.getSubItems(tab, stacks);

                    for (ItemStack stack : stacks)
                    {
                        ItemType type = new ItemType(stack);

                        if (addedItems.contains(type) == false)
                        {
                            provider.addLine(itemDump, stack, rl);
                            addedItems.add(type);
                            ++count;
                        }
                    }
                }
            }

            if (count > 0)
            {
                return;
            }
        }

        provider.addLine(itemDump, new ItemStack(item, 1, 0), rl);
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

    private static abstract class ItemInfoProviderBase
    {
        protected String getItemId(ItemStack stack)
        {
            return String.valueOf(Item.getIdFromItem(stack.getItem()));
        }

        protected String getItemMeta(ItemStack stack)
        {
            return String.valueOf(stack.isEmpty() == false ? stack.getMetadata() : 0);
        }

        protected String getModName(ResourceLocation rl)
        {
            return ModNameUtils.getModName(rl);
        }

        protected String getRegistryName(ResourceLocation rl)
        {
            return rl.toString();
        }

        protected String getDisplayName(ItemStack stack)
        {
            return stack.isEmpty() == false ? stack.getDisplayName() : DataDump.EMPTY_STRING;
        }

        protected String getHasSubtypesString(ItemStack stack)
        {
            return String.valueOf(stack.getItem().getHasSubtypes());
        }

        protected String getNBTString(ItemStack stack)
        {
            return stack.isEmpty() == false && stack.getTagCompound() != null ? stack.getTagCompound().toString() : DataDump.EMPTY_STRING;
        }

        protected String getToolClassesString(ItemStack stack)
        {
            Item item = stack.getItem();
            Set<String> toolClasses = item.getToolClasses(stack);

            if (toolClasses.isEmpty() == false)
            {
                ArrayList<String> classes = new ArrayList<>();
                classes.addAll(toolClasses);
                Collections.sort(classes);
                return String.join(", ", classes);
            }

            return "";
        }

        protected String getHarvestLevelString(ItemStack stack)
        {
            Item item = stack.getItem();
            Set<String> toolClasses = item.getToolClasses(stack);

            if (toolClasses.isEmpty() == false)
            {
                ArrayList<String> classes = new ArrayList<>();
                classes.addAll(toolClasses);
                Collections.sort(classes);

                for (int i = 0; i < classes.size(); ++i)
                {
                    String c = classes.get(i);
                    int harvestLevel = item.getHarvestLevel(stack, c, null, null);
                    String hlName = harvestLevel >= 0 && harvestLevel < HARVEST_LEVEL_NAMES.length ? HARVEST_LEVEL_NAMES[harvestLevel] : "?";
                    classes.set(i, String.format("%s = %d (%s)", c, harvestLevel, hlName));
                }

                return String.join(", ", classes);
            }

            return "";
        }

        public void addHeaders(DataDump dump)
        {
            dump.setColumnProperties(2, Alignment.RIGHT, true); // ID
            dump.setColumnProperties(3, Alignment.RIGHT, true); // meta
            dump.setColumnAlignment(4, Alignment.RIGHT); // sub-types

            dump.setUseColumnSeparator(true);

            dump.addHeader("*** WARNING ***");
            dump.addHeader("The block and item IDs are dynamic and will be different on each world!");
            dump.addHeader("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
            dump.addHeader("*** ALSO ***");
            dump.addHeader("The server doesn't have a list of sub block and sub items");
            dump.addHeader("(= items with different damage value or blocks with different metadata).");
            dump.addHeader("That is why the block and item list dumps only contain one entry per block/item class (separate ID) when run on a server.");
        }

        public abstract int getColumnCount();

        public abstract void addTitle(DataDump dump);

        public abstract void addLine(DataDump dump, ItemStack stack, ResourceLocation id);
    }

    public static class ItemInfoProviderBasic extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 7;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Ore Dict keys");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            dump.addData(this.getModName(id),
                         this.getRegistryName(id),
                         this.getItemId(stack),
                         this.getItemMeta(stack),
                         this.getHasSubtypesString(stack),
                         this.getDisplayName(stack),
                         getOredictKeysJoined(stack));
        }
    }

    public static class ItemInfoProviderNBT extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 8;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Ore Dict keys", "NBT");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            dump.addData(this.getModName(id),
                         this.getRegistryName(id),
                         this.getItemId(stack),
                         this.getItemMeta(stack),
                         this.getHasSubtypesString(stack),
                         this.getDisplayName(stack),
                         getOredictKeysJoined(stack),
                         this.getNBTString(stack));
        }
    }

    public static class ItemInfoProviderToolClasses extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 9;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Tool classes", "Harvest levels", "Ore Dict keys");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            dump.addData(this.getModName(id),
                         this.getRegistryName(id),
                         this.getItemId(stack),
                         this.getItemMeta(stack),
                         this.getHasSubtypesString(stack),
                         this.getDisplayName(stack),
                         this.getToolClassesString(stack),
                         this.getHarvestLevelString(stack),
                         getOredictKeysJoined(stack));
        }
    }

    public static class ItemInfoProviderPlantables extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 8;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Subtypes", "Display name", "Plant Type", "Ore Dict keys");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            if (stack.getItem() instanceof IPlantable)
            {
                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getItemId(stack),
                             this.getItemMeta(stack),
                             this.getHasSubtypesString(stack),
                             this.getDisplayName(stack),
                             ((IPlantable) stack.getItem()).getPlantType(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0), BlockPos.ORIGIN).name(),
                             getOredictKeysJoined(stack));
            }
        }
    }

    public static class ItemInfoProviderCraftables extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 7;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Meta/dmg", "Display name", "Recipe name", "Ore Dict keys");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());

            dump.addData(this.getModName(id),
                         this.getRegistryName(itemId),
                         this.getItemId(stack),
                         this.getItemMeta(stack),
                         this.getDisplayName(stack),
                         this.getRegistryName(id),
                         getOredictKeysJoined(stack));
        }
    }
}
