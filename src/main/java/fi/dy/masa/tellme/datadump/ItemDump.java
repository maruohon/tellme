package fi.dy.masa.tellme.datadump;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.google.common.collect.ArrayListMultimap;
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
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.mixin.IMixinAbstractFireBlock;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ItemDump
{
    public static final String[] HARVEST_LEVEL_NAMES = new String[] { "Wood/Gold", "Stone", "Iron", "Diamond" };

    public static final ItemInfoProviderBase INFO_BASIC = new ItemInfoProviderBasic(false);
    public static final ItemInfoProviderBase INFO_TAGS = new ItemInfoProviderBasic(true);
    public static final ItemInfoProviderBase INFO_CRAFTABLES = new ItemInfoProviderCraftables();
    public static final ItemInfoProviderBase INFO_DAMAGEABLES = new ItemInfoProviderDamageables();
    public static final ItemInfoProviderBase INFO_PLANTABLES = new ItemInfoProviderPlantables();

    public static List<String> getFormattedItemDump(Format format, ItemInfoProviderBase provider)
    {
        DataDump itemDump = new DataDump(provider.getColumnCount(), format);
        ItemDumpContext ctx = new ItemDumpContext(createItemTagMap());

        for (Identifier id : Registry.ITEM.getIds())
        {
            Item item = Registry.ITEM.get(id);
            provider.addLine(itemDump, new ItemStack(item), id, ctx);
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
            ItemDumpContext ctx = new ItemDumpContext(createItemTagMap());

            for (Recipe<?> recipe : manager.values())
            {
                ItemStack stack = recipe.getOutput();

                if (stack.isEmpty() == false)
                {
                    provider.addLine(dump, stack, recipe.getId(), ctx);
                }
            }
        }

        provider.addTitle(dump);
        provider.addHeaders(dump);

        return dump.getLines();
    }

    public static String getStackInfoBasic(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            Identifier rl = Registry.ITEM.getId(stack.getItem());
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = stack.getName().getString();
            displayName = Formatting.strip(displayName);

            return String.format("[%s - '%s']", regName, displayName);
        }

        return DataDump.EMPTY_STRING;
    }

    public static String getStackInfo(ItemStack stack)
    {
        if (stack.isEmpty() == false)
        {
            Identifier rl = Registry.ITEM.getId(stack.getItem());
            String regName = rl != null ? rl.toString() : "<null>";
            String displayName = stack.getName().getString();
            displayName = Formatting.strip(displayName);
            String nbt = stack.getNbt() != null ? stack.getNbt().toString() : "<no NBT>";

            return String.format("[%s - '%s' - %s]", regName, displayName, nbt);
        }

        return DataDump.EMPTY_STRING;
    }

    public static String getJsonItemsWithPropsDump(PlayerEntity player)
    {
        HashMultimap<String, Identifier> map = HashMultimap.create(10000, 16);

        // Get a mapping of modName => collection-of-block-names
        for (Identifier id : Registry.ITEM.getIds())
        {
            map.put(id.getNamespace(), id);
        }

        // First sort by mod name
        List<String> mods = Lists.newArrayList(map.keySet());
        Collections.sort(mods);
        JsonObject root = new JsonObject();
        ItemDumpContext ctx = new ItemDumpContext(createItemTagMap());

        for (String mod : mods)
        {
            // For each mod, sort the items by their registry name
            List<Identifier> items = Lists.newArrayList(map.get(mod));
            Collections.sort(items);
            JsonObject objectMod = new JsonObject();

            for (Identifier key : items)
            {
                JsonArray arrItem = new JsonArray();
                addDataForItemSubtypeForJson(arrItem, Registry.ITEM.get(key), key, player, ctx);
                objectMod.add(key.toString(), arrItem);
            }

            root.add(ModNameUtils.getModName(items.get(0)), objectMod);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(root);
    }

    private static void addDataForItemSubtypeForJson(JsonArray arr, Item item, Identifier rl, PlayerEntity player, ItemDumpContext ctx)
    {
        int id = Item.getRawId(item);
        ItemStack stack = new ItemStack(item);
        int maxDamage = stack.getMaxDamage();
        String idStr = String.valueOf(id);
        String tags = getTagNamesJoined(item, ctx.tagMap);
        String regName = rl != null ? rl.toString() : "<null>";
        String displayName = stack.getName().getString();
        displayName = Formatting.strip(displayName);

        JsonObject obj = new JsonObject();
        obj.add("RegistryName", new JsonPrimitive(regName));
        obj.add("ItemID", new JsonPrimitive(idStr));
        obj.add("MaxStackSize", new JsonPrimitive(stack.getMaxCount()));

        if (maxDamage > 0)
        {
            obj.add("MaxDurability", new JsonPrimitive(maxDamage));
        }

        obj.add("DisplayName", new JsonPrimitive(displayName));

        TellMe.dataProvider.addItemGroupNames(obj, item);

        if (item instanceof BlockItem)
        {
            try
            {
                World world = player.getEntityWorld();
                BlockPos pos = BlockPos.ORIGIN;
                Block block = ((BlockItem) item).getBlock();
                BlockState state = block.getDefaultState();
                String hardness = String.format("%.2f", state.getHardness(world, pos));
                String resistance = String.format("%.2f", block.getBlastResistance());
                boolean fallingBlock = block instanceof FallingBlock;
                int light = state.getLuminance();
                boolean flammable = ((IMixinAbstractFireBlock) Blocks.FIRE).tellme_getIsFlammable(state);
                int opacity = state.getOpacity(world, pos);

                obj.add("Type", new JsonPrimitive("block"));
                obj.add("Hardness", new JsonPrimitive(hardness));
                obj.add("Resistance", new JsonPrimitive(resistance));
                obj.add("LightValue", new JsonPrimitive(light));
                obj.add("LightOpacity", new JsonPrimitive(opacity));
                obj.add("Flammable", new JsonPrimitive(flammable));
                obj.add("FallingBlock", new JsonPrimitive(fallingBlock));
            }
            catch (Exception e) {}
        }
        else if (item.isFood())
        {
            FoodComponent food = item.getFoodComponent();
            String hunger = stack.isEmpty() == false ? String.valueOf(food.getHunger()) : "?";
            String saturation = stack.isEmpty() == false ? String.valueOf(food.getSaturationModifier()) : "?";

            obj.add("Type", new JsonPrimitive("food"));
            obj.add("Hunger", new JsonPrimitive(hunger));
            obj.add("Saturation", new JsonPrimitive(saturation));
        }
        else
        {
            obj.add("Type", new JsonPrimitive("generic"));

            Multimap<EntityAttribute, EntityAttributeModifier> attributes = item.getAttributeModifiers(EquipmentSlot.MAINHAND);

            if (attributes.isEmpty() == false)
            {
                JsonArray attributeArr = new JsonArray();

                for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : attributes.entries())
                {
                    JsonObject o1 = new JsonObject();
                    JsonObject o2 = new JsonObject();
                    o1.add("Type", new JsonPrimitive(entry.getKey().getTranslationKey()));
                    o1.add("Value", o2);

                    EntityAttributeModifier att = entry.getValue();
                    o2.add("Name", new JsonPrimitive(att.getName()));
                    o2.add("Operation", new JsonPrimitive(att.getOperation().name()));
                    o2.add("Amount", new JsonPrimitive(att.getValue()));

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
            return String.valueOf(Item.getRawId(stack.getItem()));
        }

        protected String getModName(Identifier rl)
        {
            return ModNameUtils.getModName(rl);
        }

        protected String getRegistryName(Identifier rl)
        {
            return rl.toString();
        }

        protected String getDisplayName(ItemStack stack)
        {
            return stack.isEmpty() == false ? stack.getName().getString() : DataDump.EMPTY_STRING;
        }

        protected String getNBTString(ItemStack stack)
        {
            return stack.isEmpty() == false && stack.getNbt() != null ? stack.getNbt().toString() : DataDump.EMPTY_STRING;
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

        public abstract void addLine(DataDump dump, ItemStack stack, Identifier id, ItemDumpContext ctx);
    }

    public static class ItemDumpContext
    {
        public final ArrayListMultimap<Item, Identifier> tagMap;

        public ItemDumpContext(ArrayListMultimap<Item, Identifier> tagMap)
        {
            this.tagMap = tagMap;
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
        public void addLine(DataDump dump, ItemStack stack, Identifier id, ItemDumpContext ctx)
        {
            if (this.tags)
            {
                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getItemId(stack),
                             this.getDisplayName(stack),
                             getTagNamesJoined(stack.getItem(), ctx.tagMap));
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

    public static class ItemInfoProviderPlantables extends ItemInfoProviderBase
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
        public void addLine(DataDump dump, ItemStack stack, Identifier id, ItemDumpContext ctx)
        {
            if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof Fertilizable)
            {
                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getItemId(stack),
                             this.getDisplayName(stack),
                             getTagNamesJoined(stack.getItem(), ctx.tagMap));
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
        public void addLine(DataDump dump, ItemStack stack, Identifier id, ItemDumpContext ctx)
        {
            Identifier itemId = Registry.ITEM.getId(stack.getItem());

            dump.addData(this.getModName(id),
                         this.getRegistryName(itemId),
                         this.getItemId(stack),
                         this.getDisplayName(stack),
                         this.getRegistryName(id),
                         getTagNamesJoined(stack.getItem(), ctx.tagMap));
        }
    }

    public static class ItemInfoProviderDamageables extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 6;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Durability", "Tags");
            dump.setColumnProperties(4, Alignment.RIGHT, true); // durability
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, Identifier id, ItemDumpContext ctx)
        {
            Item item = stack.getItem();

            if (item.isDamageable())
            {
                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getItemId(stack),
                             this.getDisplayName(stack),
                             String.valueOf(item.getMaxDamage()),
                             getTagNamesJoined(item, ctx.tagMap));
            }
        }
    }

    public static String getTagNamesJoined(Item item, ArrayListMultimap<Item, Identifier> tagMap)
    {
        return tagMap.get(item).stream().map(Identifier::toString).sorted().collect(Collectors.joining(", "));
    }

    public static ArrayListMultimap<Item, Identifier> createItemTagMap()
    {
        ArrayListMultimap<Item, Identifier> tagMapOut = ArrayListMultimap.create();
        Map<Identifier, Tag<Item>> tagMapIn = ItemTags.getTagGroup().getTags();

        for (Map.Entry<Identifier, Tag<Item>> entry : tagMapIn.entrySet())
        {
            final Tag<Item> tag = entry.getValue();
            final Identifier id = entry.getKey();
            tag.values().forEach((item) -> tagMapOut.put(item, id));
        }

        return tagMapOut;
    }
}
