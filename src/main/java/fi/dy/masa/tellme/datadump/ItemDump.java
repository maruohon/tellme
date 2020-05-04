package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ModNameUtils;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;

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
            provider.addLine(itemDump, new ItemStack(entry.getValue()), entry.getKey());
        }

        provider.addTitle(itemDump);
        provider.addHeaders(itemDump);

        return itemDump.getLines();
    }

    public static List<String> getFormattedCraftableItemsDump(Format format)
    {
        ItemInfoProviderBase provider = INFO_CRAFTABLES;
        DataDump dump = new DataDump(provider.getColumnCount(), format);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server != null)
        {
            RecipeManager manager = server.getRecipeManager();

            for (IRecipe<?> recipe : manager.getRecipes())
            {
                ItemStack stack = recipe.getRecipeOutput();

                if (stack.isEmpty() == false && recipe.canFit(3, 3))
                {
                    provider.addLine(dump, stack, recipe.getId());
                }
            }
        }

        provider.addTitle(dump);
        provider.addHeaders(dump);

        return dump.getLines();
    }

    public static String getTagNamesJoined(Item item)
    {
        return item.getTags().stream().map((id) -> id.toString()).sorted().collect(Collectors.joining(", "));
    }

    public static String getStackInfoBasic(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            ResourceLocation rl = stack.getItem().getRegistryName();
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = stack.getDisplayName().getString();
            displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

            return String.format("[%s - '%s']", regName, displayName);
        }

        return DataDump.EMPTY_STRING;
    }

    public static String getStackInfo(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            ResourceLocation rl = stack.getItem().getRegistryName();
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = stack.getDisplayName().getString();
            displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);
            String nbt = stack.getTag() != null ? stack.getTag().toString() : "<no NBT>";

            return String.format("[%s - '%s' - %s]", regName, displayName, nbt);
        }

        return DataDump.EMPTY_STRING;
    }

    public static String getJsonItemsWithPropsDump(PlayerEntity player)
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
                addDataForItemSubtypeForJson(arrItem, ForgeRegistries.ITEMS.getValue(key), key, player);
                objectMod.add(key.toString(), arrItem);
            }

            root.add(ModNameUtils.getModName(items.get(0)), objectMod);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(root);
    }

    private static void addDataForItemSubtypeForJson(JsonArray arr, Item item, ResourceLocation rl, PlayerEntity player)
    {
        int id = Item.getIdFromItem(item);
        ItemStack stack = new ItemStack(item);
        int maxDamage = stack.getMaxDamage();
        String idStr = String.valueOf(id);
        String exists = DataDump.isDummied(ForgeRegistries.ITEMS, rl) ? "false" : "true";
        String tags = getTagNamesJoined(item);
        String regName = rl != null ? rl.toString() : "<null>";
        String displayName = stack.getDisplayName().getString();
        displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

        JsonObject obj = new JsonObject();
        obj.add("RegistryName", new JsonPrimitive(regName));
        obj.add("ItemID", new JsonPrimitive(idStr));
        obj.add("MaxStackSize", new JsonPrimitive(stack.getMaxStackSize()));

        if (maxDamage > 0)
        {
            obj.add("MaxDurability", new JsonPrimitive(maxDamage));
        }

        obj.add("Exists", new JsonPrimitive(exists));
        obj.add("DisplayName", new JsonPrimitive(displayName));

        TellMe.dataProvider.addItemGroupNames(obj, item);

        if (item instanceof BlockItem)
        {
            try
            {
                World world = player.getEntityWorld();
                BlockPos pos = BlockPos.ZERO;
                Block block = ((BlockItem) item).getBlock();
                BlockState state = block.getDefaultState();
                String hardness = String.format("%.2f", BlockDump.field_blockHardness.get(block));
                String resistance = String.format("%.2f", BlockDump.field_blockResistance.get(block));
                String tool = block.getHarvestTool(state).getName();
                int harvestLevel = block.getHarvestLevel(state);
                String harvestLevelName = (harvestLevel >= 0 && harvestLevel < HARVEST_LEVEL_NAMES.length) ? HARVEST_LEVEL_NAMES[harvestLevel] : "Unknown";
                boolean fallingBlock = block instanceof FallingBlock;
                int light = state.getLightValue();
                // Ugly way to try to get the flammability...
                boolean flammable = block.getFlammability(state, world, pos, Direction.UP) > 0;//Blocks.FIRE.getFlammability(block) > 0;
                int opacity = state.getOpacity(world, pos);

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
        else if (item.isFood())
        {
            Food food = item.getFood();
            String hunger = stack.isEmpty() == false ? String.valueOf(food.getHealing()) : "?";
            String saturation = stack.isEmpty() == false ? String.valueOf(food.getSaturation()) : "?";

            obj.add("Type", new JsonPrimitive("food"));
            obj.add("Hunger", new JsonPrimitive(hunger));
            obj.add("Saturation", new JsonPrimitive(saturation));
        }
        else
        {
            obj.add("Type", new JsonPrimitive("generic"));
            List<ToolType> toolTypes = new ArrayList<>(item.getToolTypes(stack));

            if (toolTypes.isEmpty() == false)
            {
                ArrayList<String> toolTypeNames = new ArrayList<>();
                toolTypes.forEach((c) -> toolTypeNames.add(c.getName()));
                Collections.sort(toolTypeNames);

                String levels = toolTypes.stream().map((t) -> String.valueOf(item.getHarvestLevel(stack, t, player, null))).collect(Collectors.joining(","));

                obj.add("ToolTypes", new JsonPrimitive(String.join(",", toolTypeNames)));
                obj.add("HarvestLevels", new JsonPrimitive(levels));

                if (item instanceof ToolItem)
                {
                    obj.add("ToolMaterial", new JsonPrimitive(((ToolItem) item).getTier().toString()));
                }
            }

            Multimap<String, AttributeModifier> attributes = item.getAttributeModifiers(EquipmentSlotType.MAINHAND, stack);

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
                    o2.add("Operation", new JsonPrimitive(att.getOperation().name()));
                    o2.add("Amount", new JsonPrimitive(att.getAmount()));

                    attributeArr.add(o1);
                }

                obj.add("Attributes", attributeArr);
            }
        }

        obj.add("Tags", new JsonPrimitive(tags));

        arr.add(obj);
    }

    public static void setHeldItemWithoutEquipSound(PlayerEntity player, Hand hand, ItemStack stack)
    {
        if (hand == Hand.MAIN_HAND)
        {
            player.inventory.mainInventory.set(player.inventory.currentItem, stack);
        }
        else if (hand == Hand.OFF_HAND)
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
            return stack.isEmpty() == false ? stack.getDisplayName().getString() : DataDump.EMPTY_STRING;
        }

        protected String getNBTString(ItemStack stack)
        {
            return stack.isEmpty() == false && stack.getTag() != null ? stack.getTag().toString() : DataDump.EMPTY_STRING;
        }

        public void addHeaders(DataDump dump)
        {
            dump.setColumnProperties(2, Alignment.RIGHT, true); // ID

            dump.setUseColumnSeparator(true);

            dump.addHeader("*** WARNING ***");
            dump.addHeader("Don't use the item ID for anything \"proper\"!!");
            dump.addHeader("It's provided here for completeness's sake, it's different in every world.");
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
            return 5;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Tags");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            dump.addData(this.getModName(id),
                         this.getRegistryName(id),
                         this.getItemId(stack),
                         this.getDisplayName(stack),
                         getTagNamesJoined(stack.getItem()));
        }
    }

    public static class ItemInfoProviderNBT extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 6;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Tags", "NBT");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            dump.addData(this.getModName(id),
                         this.getRegistryName(id),
                         this.getItemId(stack),
                         this.getDisplayName(stack),
                         getTagNamesJoined(stack.getItem()),
                         this.getNBTString(stack));
        }
    }

    public static class ItemInfoProviderToolClasses extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 7;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Tool classes", "Harvest levels", "Tags");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            Item item = stack.getItem();

            if (item.getToolTypes(stack).isEmpty() == false)
            {
                List<ToolType> toolTypes = new ArrayList<>(item.getToolTypes(stack));

                Collections.sort(toolTypes, (t1, t2) -> t1.getName().compareTo(t2.getName()));

                List<String> strings = new ArrayList<>();
                toolTypes.forEach((c) -> strings.add(c.getName()));

                String toolClasses = String.join(", ", strings);

                for (int i = 0; i < toolTypes.size(); ++i)
                {
                    ToolType type = toolTypes.get(i);
                    int harvestLevel = item.getHarvestLevel(stack, type, null, null);
                    String hlName = harvestLevel >= 0 && harvestLevel < HARVEST_LEVEL_NAMES.length ? HARVEST_LEVEL_NAMES[harvestLevel] : "?";
                    strings.set(i, String.format("%s = %d (%s)", type.getName(), harvestLevel, hlName));
                }

                String harvestLevels = String.join(", ", strings);

                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getItemId(stack),
                             this.getDisplayName(stack),
                             toolClasses,
                             harvestLevels,
                             getTagNamesJoined(stack.getItem()));
            }
        }
    }

    public static class ItemInfoProviderPlantables extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 6;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Plant Type", "Tags");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof IPlantable)
            {
                World world = null;

                try
                {
                    world = TellMe.dataProvider.getWorld(ServerLifecycleHooks.getCurrentServer(), DimensionType.OVERWORLD);
                }
                catch (Exception ignore) {}

                if (world != null)
                {
                    Block block = ((BlockItem) stack.getItem()).getBlock();

                    dump.addData(this.getModName(id),
                            this.getRegistryName(id),
                            this.getItemId(stack),
                            this.getDisplayName(stack),
                            ((IPlantable) block).getPlantType(world, BlockPos.ZERO).name(),
                            getTagNamesJoined(stack.getItem()));
                }
            }
        }
    }

    public static class ItemInfoProviderCraftables extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 6;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Recipe name", "Tags");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());

            dump.addData(this.getModName(id),
                         this.getRegistryName(itemId),
                         this.getItemId(stack),
                         this.getDisplayName(stack),
                         this.getRegistryName(id),
                         getTagNamesJoined(stack.getItem()));
        }
    }
}
