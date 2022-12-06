package fi.dy.masa.tellme.datadump;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.ForgeRegistries;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class BlockDump
{
    public static List<String> getFormattedBlockDump(DataDump.Format format, boolean tags)
    {
        DataDump blockDump = new DataDump(tags ? 5 : 4, format);

        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            Block block = entry.getValue();
            addDataToDump(blockDump, entry.getKey().location(), block, new ItemStack(entry.getValue()), tags);
        }

        if (tags)
        {
            blockDump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Tags");
        }
        else
        {
            blockDump.addTitle("Mod name", "Registry name", "Item ID", "Display name");
        }

        return blockDump.getLines();
    }

    public static List<String> getFormattedBlockToMapColorDump(DataDump.Format format, @Nullable Level world)
    {
        DataDump blockDump = new DataDump(3, format);

        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            try
            {
                Block block = entry.getValue();
                String id = entry.getKey().location().toString();
                MaterialColor materialColor = block.defaultBlockState().getMapColor(world, BlockPos.ZERO);
                int color = materialColor != null ? materialColor.col : 0xFFFFFF;
                blockDump.addData(id, String.format("#%06X", color), String.valueOf(color));
            }
            catch (Exception ignore) {}
        }

        blockDump.addTitle("Registry name", "Map color (hex)", "Map color (int)");

        return blockDump.getLines();
    }

    private static void addDataToDump(DataDump dump, ResourceLocation id, Block block, ItemStack stack, boolean tags)
    {
        String modName = ModNameUtils.getModName(id);
        String registryName = id.toString();
        String displayName = stack.isEmpty() == false ? stack.getHoverName().getString() : (Component.translatable(block.getDescriptionId())).getString();
        displayName = ChatFormatting.stripFormatting(displayName);
        Item item = stack.getItem();
        ResourceLocation itemIdRl = item != Items.AIR ? ForgeRegistries.ITEMS.getKey(item) : null;
        String itemId = itemIdRl != null ? itemIdRl.toString() : DataDump.EMPTY_STRING;

        if (tags)
        {
            dump.addData(modName, registryName, itemId, displayName, getTagNamesJoined(block));
        }
        else
        {
            dump.addData(modName, registryName, itemId, displayName);
        }
    }

    public static List<String> getFormattedBlockPropertiesDump(DataDump.Format format)
    {
        DataDump blockDump = new DataDump(4, format);

        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            ResourceLocation id = entry.getKey().location();

            try
            {
                Block block = entry.getValue();
                String modName = ModNameUtils.getModName(id);
                String hardness = String.format("%.2f", block.defaultBlockState().getDestroySpeed(null, BlockPos.ZERO));
                @SuppressWarnings("deprecation")
                String resistance = String.format("%.2f", block.getExplosionResistance());
                blockDump.addData(modName, id.toString(), hardness, resistance);
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Exception while trying to get block-props dump for '{}'", id);
            }
        }

        blockDump.addTitle("Mod name", "Registry name", "Hardness", "Resistance");

        blockDump.setColumnProperties(2, Alignment.RIGHT, true); // Hardness
        blockDump.setColumnProperties(3, Alignment.RIGHT, true); // Resistance

        blockDump.addHeader("NOTE: The Hardness and Resistance values are the raw base values in the fields");
        blockDump.addHeader("of the Block class in question. The actual final values may be different");
        blockDump.addHeader("for different states of the block, or they may depend on a BlockEntity etc.");

        blockDump.addFooter("NOTE: The Hardness and Resistance values are the raw base values in the fields");
        blockDump.addFooter("of the Block class in question. The actual final values may be different");
        blockDump.addFooter("for different states of the block, or they may depend on a BlockEntity etc.");

        return blockDump.getLines();
    }

    public static String getJsonBlockDump()
    {
        HashMultimap<String, ResourceLocation> map = HashMultimap.create(400, 512);

        // Get a mapping of modName => collection-of-block-names
        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(entry.getValue());
            map.put(id.getNamespace(), id);
        }

        // First sort by mod name
        List<String> modIds = Lists.newArrayList(map.keySet());
        Collections.sort(modIds);
        JsonObject root = new JsonObject();

        for (String mod : modIds)
        {
            // For each mod, sort the blocks by their registry name
            List<ResourceLocation> blockIds = Lists.newArrayList(map.get(mod));
            Collections.sort(blockIds);
            JsonObject objectMod = new JsonObject();

            for (ResourceLocation key : blockIds)
            {
                JsonObject objBlock = new JsonObject();

                String registryName = key.toString();
                Block block = ForgeRegistries.BLOCKS.getValue(key);
                ItemStack stack = new ItemStack(block);
                Item item = stack.getItem();

                objBlock.add("RegistryName", new JsonPrimitive(registryName));

                if (item != null && item != Items.AIR)
                {
                    ResourceLocation itemIdRl = item != Items.AIR ? ForgeRegistries.ITEMS.getKey(item) : null;
                    String itemId = itemIdRl != null ? itemIdRl.toString() : DataDump.EMPTY_STRING;

                    String displayName = stack.isEmpty() == false ? stack.getHoverName().getString() : (Component.translatable(block.getDescriptionId())).getString();
                    displayName = ChatFormatting.stripFormatting(displayName);

                    JsonObject objItem = new JsonObject();
                    objItem.add("RegistryName", new JsonPrimitive(itemId));
                    objItem.add("DisplayName", new JsonPrimitive(displayName));

                    String tags = getTagNamesJoined(block);
                    objItem.add("Tags", new JsonPrimitive(tags));

                    objBlock.add("Item", objItem);
                }

                objectMod.add(key.toString(), objBlock);
            }

            root.add(ModNameUtils.getModName(blockIds.get(0)), objectMod);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(root);
    }

    @SuppressWarnings("deprecation")
    public static String getTagNamesJoined(Block block)
    {
        return block.builtInRegistryHolder().getTagKeys().map(e -> e.location().toString()).collect(Collectors.joining(", "));
    }

    public static String getRegistryName(Block block)
    {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return id != null ? id.toString() : "<null>";
    }
}
