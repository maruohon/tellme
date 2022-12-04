package fi.dy.masa.tellme.datadump;

import java.util.Collections;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.registries.ForgeRegistries;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.RegistryUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ItemDump
{
    public static final ItemInfoProviderBase INFO_BASIC = new ItemInfoProviderBasic(false);
    public static final ItemInfoProviderBase INFO_TAGS = new ItemInfoProviderBasic(true);
    public static final ItemInfoProviderBase INFO_CRAFTABLES = new ItemInfoProviderCraftables();
    public static final ItemInfoProviderBase INFO_DAMAGEABLES = new ItemInfoProviderDamageables();
    public static final ItemInfoProviderBase INFO_PLANTABLES = new ItemInfoProviderPlantables();
    public static final ItemInfoProviderBase INFO_TIERED = new ItemInfoProviderTiered();

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
        return item.builtInRegistryHolder().getTagKeys().map(e -> e.location().toString()).collect(Collectors.joining(", "));
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
        }

        public abstract int getColumnCount();

        public abstract void addTitle(DataDump dump);

        public abstract void addLine(DataDump dump, ItemStack stack, ResourceLocation id);
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
        public void addHeaders(DataDump dump)
        {
            dump.setColumnProperties(2, Alignment.RIGHT, true); // ID

            dump.addHeader("*** WARNING ***");
            dump.addHeader("Don't use the item ID for anything \"proper\"!!");
            dump.addHeader("It's provided here for completeness's sake, it's different in every world.");
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

    public static class ItemInfoProviderTiered extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 4;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Display name", "Tier");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            Item item = stack.getItem();

            if (item instanceof TieredItem tiered)
            {
                Tier tier = tiered.getTier();
                @SuppressWarnings("deprecation")
                String tierInfo = String.format("uses: %d, level: %d, speed: %.3f, dmgbns: %.3f, ench: %d",
                                                tier.getUses(), tier.getLevel(), tier.getSpeed(),
                                                tier.getAttackDamageBonus(), tier.getEnchantmentValue());

                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getDisplayName(stack),
                             tierInfo);
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
            dump.addTitle("Mod name", "Registry name", "Display name", "Plant Type", "Tags");
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
            dump.addTitle("Mod name", "Registry name", "Display name", "Max Damage", "Tags");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            if (stack.isDamageableItem())
            {
                dump.addData(this.getModName(id),
                             this.getRegistryName(id),
                             this.getDisplayName(stack),
                             String.valueOf(stack.getMaxDamage()),
                             getTagNamesJoined(stack.getItem()));
            }
        }
    }

    public static class ItemInfoProviderCraftables extends ItemInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 5;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("Mod name", "Registry name", "Display name", "Recipe name", "Tags");
        }

        @Override
        public void addLine(DataDump dump, ItemStack stack, ResourceLocation id)
        {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());

            dump.addData(this.getModName(id),
                         this.getRegistryName(itemId),
                         this.getDisplayName(stack),
                         this.getRegistryName(id),
                         getTagNamesJoined(stack.getItem()));
        }
    }
}
