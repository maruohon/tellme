package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.RegistryUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ItemDump
{
    public static final String[] HARVEST_LEVEL_NAMES = new String[] { "Wood/Gold", "Stone", "Iron", "Diamond" };

    public static final ItemInfoProviderBase INFO_REGISTRY_NAME = new ItemInfoProviderRegistryNameOnly();
    public static final ItemInfoProviderBase INFO_BASIC = new ItemInfoProviderBasic(false);
    public static final ItemInfoProviderBase INFO_TAGS = new ItemInfoProviderBasic(true);
    public static final ItemInfoProviderBase INFO_CRAFTABLES = new ItemInfoProviderCraftables();
    public static final ItemInfoProviderBase INFO_DAMAGEABLES = new ItemInfoProviderDamageables();
    public static final ItemInfoProviderBase INFO_PLANTABLES = new ItemInfoProviderPlantables();
    public static final ItemInfoProviderBase INFO_TOOL_CLASS = new ItemInfoProviderToolClasses();

    public static List<String> getFormattedItemDump(Format format, ItemInfoProviderBase provider)
    {
        DataDump itemDump = new DataDump(provider.getColumnCount(), format);

        for (Map.Entry<ResourceKey<Item>, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            Item item = entry.getValue();
            provider.addLine(itemDump, new ItemStack(item), item.getRegistryName());
        }

        provider.addTitle(itemDump);
        provider.addHeaders(itemDump);

        return itemDump.getLines();
    }

    public static List<String> getFormattedCraftableItemsDump(Format format, @Nullable MinecraftServer server)
    {
        ItemInfoProviderBase provider = INFO_CRAFTABLES;
        DataDump dump = new DataDump(provider.getColumnCount(), format);

        if (server != null)
        {
            RecipeManager manager = server.getRecipeManager();

            for (Recipe<?> recipe : manager.getRecipes())
            {
                ItemStack stack = recipe.getResultItem();

                if (stack.isEmpty() == false && recipe.canCraftInDimensions(3, 3))
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
        return item.getTags().stream().map(ResourceLocation::toString).sorted().collect(Collectors.joining(", "));
    }

    public static String getStackInfoBasic(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            ResourceLocation rl = stack.getItem().getRegistryName();
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = stack.getHoverName().getString();
            displayName = ChatFormatting.stripFormatting(displayName);

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
            String displayName = stack.getHoverName().getString();
            displayName = ChatFormatting.stripFormatting(displayName);
            String nbt = stack.getTag() != null ? stack.getTag().toString() : "<no NBT>";

            return String.format("[%s - '%s' - %s]", regName, displayName, nbt);
        }

        return DataDump.EMPTY_STRING;
    }

    public static String getJsonItemsWithPropsDump(Player player)
    {
        HashMultimap<String, ResourceLocation> map = HashMultimap.create(10000, 16);

        // Get a mapping of modName => collection-of-block-names
        for (Map.Entry<ResourceKey<Item>, Item> entry : ForgeRegistries.ITEMS.getEntries())
        {
            ResourceLocation key = entry.getValue().getRegistryName();
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

    private static void addDataForItemSubtypeForJson(JsonArray arr, Item item, ResourceLocation rl, Player player)
    {
        int id = Item.getId(item);
        ItemStack stack = new ItemStack(item);
        int maxDamage = stack.getMaxDamage();
        String idStr = String.valueOf(id);
        String exists = RegistryUtils.isDummied(ForgeRegistries.ITEMS, rl) ? "false" : "true";
        String tags = getTagNamesJoined(item);
        String regName = rl != null ? rl.toString() : "<null>";
        String displayName = stack.getHoverName().getString();
        displayName = ChatFormatting.stripFormatting(displayName);

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
                Level world = player.getCommandSenderWorld();
                BlockPos pos = BlockPos.ZERO;
                Block block = ((BlockItem) item).getBlock();
                BlockState state = block.defaultBlockState();
                String hardness = String.format("%.2f", block.defaultBlockState().getDestroySpeed(null, BlockPos.ZERO));
                @SuppressWarnings("deprecation")
                String resistance = String.format("%.2f", block.getExplosionResistance());
                String tool = block.getHarvestTool(state).getName();
                int harvestLevel = block.getHarvestLevel(state);
                String harvestLevelName = (harvestLevel >= 0 && harvestLevel < HARVEST_LEVEL_NAMES.length) ? HARVEST_LEVEL_NAMES[harvestLevel] : "Unknown";
                boolean fallingBlock = block instanceof FallingBlock;
                @SuppressWarnings("deprecation")
                int light = state.getLightEmission();
                // Ugly way to try to get the flammability...
                boolean flammable = block.getFlammability(state, world, pos, Direction.UP) > 0;//Blocks.FIRE.getFlammability(block) > 0;
                int opacity = state.getLightBlock(world, pos);

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
            catch (Exception ignored) {}
        }
        else if (item.isEdible())
        {
            FoodProperties food = item.getFoodProperties();
            String hunger = stack.isEmpty() == false ? String.valueOf(food.getNutrition()) : "?";
            String saturation = stack.isEmpty() == false ? String.valueOf(food.getSaturationModifier()) : "?";

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

                if (item instanceof DiggerItem)
                {
                    obj.add("ToolMaterial", new JsonPrimitive(((DiggerItem) item).getTier().toString()));
                }
            }

            Multimap<Attribute, AttributeModifier> attributes = item.getAttributeModifiers(EquipmentSlot.MAINHAND, stack);

            if (attributes.isEmpty() == false)
            {
                JsonArray attributeArr = new JsonArray();

                for (Entry<Attribute, AttributeModifier> entry : attributes.entries())
                {
                    JsonObject o1 = new JsonObject();
                    JsonObject o2 = new JsonObject();
                    o1.add("Type", new JsonPrimitive(entry.getKey().getDescriptionId()));
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

    private static abstract class ItemInfoProviderBase
    {
        protected String getItemId(ItemStack stack)
        {
            return String.valueOf(Item.getId(stack.getItem()));
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
            return stack.isEmpty() == false ? stack.getHoverName().getString() : DataDump.EMPTY_STRING;
        }

        protected String getNBTString(ItemStack stack)
        {
            return stack.isEmpty() == false && stack.getTag() != null ? stack.getTag().toString() : DataDump.EMPTY_STRING;
        }

        public void addHeaders(DataDump dump)
        {
            dump.setColumnProperties(2, Alignment.RIGHT, true); // ID

            dump.addHeader("*** WARNING ***");
            dump.addHeader("Don't use the item ID for anything \"proper\"!!");
            dump.addHeader("It's provided here for completeness's sake, it's different in every world.");
        }

        public abstract int getColumnCount();

        public abstract void addTitle(DataDump dump);

        public abstract void addLine(DataDump dump, ItemStack stack, ResourceLocation id);
    }

    public static class ItemInfoProviderRegistryNameOnly extends ItemInfoProviderBase
    {
        public ItemInfoProviderRegistryNameOnly()
        {
        }

        @Override
        public int getColumnCount()
        {
            return 1;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Registry name");
        }

        @Override
        public void addHeaders(DataDump dump)
        {
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            dump.addData(this.getRegistryName(id));
        }
    }

    public static class ItemInfoProviderBasic extends ItemInfoProviderBase
    {
        private final boolean tags;

        public ItemInfoProviderBasic(boolean tags)
        {
            this.tags = tags;
        }

        @Override
        public int getColumnCount()
        {
            return this.tags ? 5 : 4;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            if (this.tags)
            {
                dump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Tags");
            }
            else
            {
                dump.addTitle("Mod name", "Registry name", "Item ID", "Display name");
            }
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            if (this.tags)
            {
                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getItemId(stack),
                             this.getDisplayName(stack),
                             getTagNamesJoined(stack.getItem()));
            }
            else
            {
                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getItemId(stack),
                             this.getDisplayName(stack));
            }
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

                Collections.sort(toolTypes, Comparator.comparing(ToolType::getName));

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
                try
                {
                    Block block = ((BlockItem) stack.getItem()).getBlock();

                    dump.addData(this.getModName(id),
                                 this.getRegistryName(id),
                                 this.getItemId(stack),
                                 this.getDisplayName(stack),
                                 ((IPlantable) block).getPlantType(null, BlockPos.ZERO).getName(),
                                 getTagNamesJoined(stack.getItem()));
                }
                catch (Exception ignore)
                {
                    TellMe.logger.warn("Exception while trying to get plant type for '{}'", this.getRegistryName(id));
                }
            }
        }
    }

    public static class ItemInfoProviderDamageables extends ItemInfoProviderBase
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
            if (stack.getItem().canBeDepleted())
            {
                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getItemId(stack),
                             this.getDisplayName(stack),
                             getTagNamesJoined(stack.getItem()));
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
