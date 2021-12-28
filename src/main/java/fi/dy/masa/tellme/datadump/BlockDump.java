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
import net.minecraft.block.Block;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.RegistryUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class BlockDump
{
    public static List<String> getFormattedBlockDump(DataDump.Format format, boolean tags)
    {
        DataDump blockDump = new DataDump(tags ? 6 : 5, format);

        for (Map.Entry<RegistryKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            Block block = entry.getValue();
            addDataToDump(blockDump, block.getRegistryName(), block, new ItemStack(entry.getValue()), tags);
        }

        if (tags)
        {
            blockDump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Exists", "Tags");
        }
        else
        {
            blockDump.addTitle("Mod name", "Registry name", "Item ID", "Display name", "Exists");
        }

        return blockDump.getLines();
    }

    public static List<String> getFormattedBlockToMapColorDump(DataDump.Format format, @Nullable World world)
    {
        DataDump blockDump = new DataDump(3, format);

        for (Map.Entry<RegistryKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            try
            {
                Block block = entry.getValue();
                ResourceLocation id = block.getRegistryName();
                MaterialColor materialColor = block.defaultBlockState().getMapColor(world, BlockPos.ZERO);
                int color = materialColor != null ? materialColor.col : 0xFFFFFF;
                blockDump.addData(id.toString(), String.format("#%06X", color), String.valueOf(color));
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
        String displayName = stack.isEmpty() == false ? stack.getHoverName().getString() : (new TranslationTextComponent(block.getDescriptionId())).getString();
        displayName = TextFormatting.stripFormatting(displayName);
        Item item = stack.getItem();
        ResourceLocation itemIdRl = item != Items.AIR ? item.getRegistryName() : null;
        String itemId = itemIdRl != null ? itemIdRl.toString() : DataDump.EMPTY_STRING;
        String exists = RegistryUtils.isDummied(ForgeRegistries.BLOCKS, id) ? "false" : "true";

        if (tags)
        {
            dump.addData(modName, registryName, itemId, displayName, exists, getTagNamesJoined(block));
        }
        else
        {
            dump.addData(modName, registryName, itemId, displayName, exists);
        }
    }

    public static List<String> getFormattedBlockPropertiesDump(DataDump.Format format)
    {
        DataDump blockDump = new DataDump(4, format);

        for (ResourceLocation id : ForgeRegistries.BLOCKS.getKeys())
        {
            try
            {
                String modName = ModNameUtils.getModName(id);
                String registryName = id.toString();
                Block block = ForgeRegistries.BLOCKS.getValue(id);
                String hardness = String.format("%.2f", block.defaultBlockState().getDestroySpeed(null, BlockPos.ZERO));
                @SuppressWarnings("deprecation")
                String resistance = String.format("%.2f", block.getExplosionResistance());
                blockDump.addData(modName, registryName, hardness, resistance);
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
        blockDump.addHeader("for different states of the block, or they may depend on a TileEntity etc.");

        blockDump.addFooter("NOTE: The Hardness and Resistance values are the raw base values in the fields");
        blockDump.addFooter("of the Block class in question. The actual final values may be different");
        blockDump.addFooter("for different states of the block, or they may depend on a TileEntity etc.");

        return blockDump.getLines();
    }

    public static String getJsonBlockDump()
    {
        HashMultimap<String, ResourceLocation> map = HashMultimap.create(400, 512);

        // Get a mapping of modName => collection-of-block-names
        for (Map.Entry<RegistryKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            ResourceLocation key = entry.getValue().getRegistryName();
            map.put(key.getNamespace(), key);
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
                String exists = RegistryUtils.isDummied(ForgeRegistries.BLOCKS, key) ? "false" : "true";

                objBlock.add("RegistryName", new JsonPrimitive(registryName));
                objBlock.add("Exists", new JsonPrimitive(exists));

                if (item != null && item != Items.AIR)
                {
                    ResourceLocation itemIdRl = item != Items.AIR ? item.getRegistryName() : null;
                    String itemId = itemIdRl != null ? itemIdRl.toString() : DataDump.EMPTY_STRING;

                    String displayName = stack.isEmpty() == false ? stack.getHoverName().getString() : (new TranslationTextComponent(block.getDescriptionId())).getString();
                    displayName = TextFormatting.stripFormatting(displayName);

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

    public static String getTagNamesJoined(Block block)
    {
        return block.getTags().stream().map(ResourceLocation::toString).sorted().collect(Collectors.joining(", "));
    }
}
