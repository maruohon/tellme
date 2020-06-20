package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.RegistryUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockDump
{
    public static final Field field_blockHardness = ObfuscationReflectionHelper.findField(Block.class, "field_149782_v"); // blockHardness
    public static final Field field_blockResistance = ObfuscationReflectionHelper.findField(Block.class, "field_149781_w"); // blockResistance

    public static List<String> getFormattedBlockDump(DataDump.Format format, boolean tags)
    {
        DataDump blockDump = new DataDump(tags ? 6 : 5, format);

        for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            addDataToDump(blockDump, entry.getKey(), entry.getValue(), new ItemStack(entry.getValue()), tags);
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

    private static void addDataToDump(DataDump dump, ResourceLocation id, Block block, ItemStack stack, boolean tags)
    {
        String modName = ModNameUtils.getModName(id);
        String registryName = id.toString();
        String displayName = stack.isEmpty() == false ? stack.getDisplayName().getString() : (new TranslationTextComponent(block.getTranslationKey())).getString();
        displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);
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

        try
        {
            for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries())
            {
                ResourceLocation rl = entry.getKey();
                String modName = ModNameUtils.getModName(rl);
                String registryName = rl.toString();
                Block block = entry.getValue();
                String hardness = String.format("%.2f", field_blockHardness.get(block));
                String resistance = String.format("%.2f", field_blockResistance.get(block));
                blockDump.addData(modName, registryName, hardness, resistance);
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
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Exception while trying to get block-props dump", e);
        }

        return blockDump.getLines();
    }

    public static String getJsonBlockDump()
    {
        HashMultimap<String, ResourceLocation> map = HashMultimap.create(400, 512);

        // Get a mapping of modName => collection-of-block-names
        for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            ResourceLocation key = entry.getKey();
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

                    String displayName = stack.isEmpty() == false ? stack.getDisplayName().getString() : (new TranslationTextComponent(block.getTranslationKey())).getString();
                    displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

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
        return block.getTags().stream().map((id) -> id.toString()).sorted().collect(Collectors.joining(", "));
    }
}
