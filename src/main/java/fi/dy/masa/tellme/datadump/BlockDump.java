package fi.dy.masa.tellme.datadump;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class BlockDump
{
    public static List<String> getFormattedBlockDump(DataDump.Format format, boolean tags)
    {
        DataDump blockDump = new DataDump(tags ? 5 : 4, format);
        ArrayListMultimap<Block, Identifier> tagMap = createBlockTagMap();

        for (Identifier id : Registry.BLOCK.getIds())
        {
            Block block = Registry.BLOCK.get(id);
            addDataToDump(blockDump, id, block, new ItemStack(block), tags, tagMap);
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

    public static List<String> getFormattedBlockToMapColorDump(DataDump.Format format, World world)
    {
        DataDump blockDump = new DataDump(3, format);

        for (Identifier id : Registry.BLOCK.getIds())
        {
            try
            {
                Block block = Registry.BLOCK.get(id);
                MapColor materialColor = block.getDefaultState().getTopMaterialColor(world, BlockPos.ORIGIN);
                int color = materialColor != null ? materialColor.color : 0xFFFFFF;
                blockDump.addData(id.toString(), String.format("#%06X", color), String.valueOf(color));
            }
            catch (Exception ignore) {}
        }

        blockDump.addTitle("Registry name", "Map color (hex)", "Map color (int)");

        return blockDump.getLines();
    }

    private static void addDataToDump(DataDump dump, Identifier id, Block block, ItemStack stack, boolean tags, ArrayListMultimap<Block, Identifier> tagMap)
    {
        String modName = ModNameUtils.getModName(id);
        String registryName = id.toString();
        String displayName = stack.isEmpty() == false ? stack.getName().getString() : (new TranslatableText(block.getTranslationKey())).getString();
        displayName = Formatting.strip(displayName);
        Item item = stack.getItem();
        Identifier itemIdRl = item != Items.AIR ? Registry.ITEM.getId(item) : null;
        String itemId = itemIdRl != null ? itemIdRl.toString() : DataDump.EMPTY_STRING;

        if (tags)
        {
            dump.addData(modName, registryName, itemId, displayName, getTagNamesJoined(block, tagMap));
        }
        else
        {
            dump.addData(modName, registryName, itemId, displayName);
        }
    }

    public static List<String> getFormattedBlockPropertiesDump(DataDump.Format format)
    {
        DataDump blockDump = new DataDump(4, format);

        for (Identifier id : Registry.BLOCK.getIds())
        {
            try
            {
                String modName = ModNameUtils.getModName(id);
                String registryName = id.toString();
                Block block = Registry.BLOCK.get(id);
                String hardness = String.format("%.2f", block.getDefaultState().getHardness(null, BlockPos.ORIGIN));
                String resistance = String.format("%.2f", block.getBlastResistance());
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
        HashMultimap<String, Identifier> map = HashMultimap.create(400, 512);

        // Get a mapping of modName => collection-of-block-names
        for (Identifier id : Registry.BLOCK.getIds())
        {
            map.put(id.getNamespace(), id);
        }

        // First sort by mod name
        List<String> modIds = Lists.newArrayList(map.keySet());
        Collections.sort(modIds);
        JsonObject root = new JsonObject();
        ArrayListMultimap<Block, Identifier> tagMap = createBlockTagMap();

        for (String mod : modIds)
        {
            // For each mod, sort the blocks by their registry name
            List<Identifier> blockIds = Lists.newArrayList(map.get(mod));
            Collections.sort(blockIds);
            JsonObject objectMod = new JsonObject();

            for (Identifier key : blockIds)
            {
                JsonObject objBlock = new JsonObject();

                String registryName = key.toString();
                Block block = Registry.BLOCK.get(key);
                ItemStack stack = new ItemStack(block);
                Item item = stack.getItem();

                objBlock.add("RegistryName", new JsonPrimitive(registryName));

                if (item != null && item != Items.AIR)
                {
                    Identifier itemIdRl = item != Items.AIR ? Registry.ITEM.getId(item) : null;
                    String itemId = itemIdRl != null ? itemIdRl.toString() : DataDump.EMPTY_STRING;

                    String displayName = stack.isEmpty() == false ? stack.getName().getString() : (new TranslatableText(block.getTranslationKey())).getString();
                    displayName = Formatting.strip(displayName);

                    JsonObject objItem = new JsonObject();
                    objItem.add("RegistryName", new JsonPrimitive(itemId));
                    objItem.add("DisplayName", new JsonPrimitive(displayName));

                    String tags = getTagNamesJoined(block, tagMap);
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

    public static String getTagNamesJoined(Block block, ArrayListMultimap<Block, Identifier> tagMap)
    {
        return tagMap.get(block).stream().map(Identifier::toString).sorted().collect(Collectors.joining(", "));
    }

    public static ArrayListMultimap<Block, Identifier> createBlockTagMap()
    {
        ArrayListMultimap<Block, Identifier> tagMapOut = ArrayListMultimap.create();
        Map<Identifier, Tag<Block>> tagMapIn = BlockTags.getTagGroup().getTags();

        for (Map.Entry<Identifier, Tag<Block>> entry : tagMapIn.entrySet())
        {
            final Tag<Block> tag = entry.getValue();
            final Identifier id = entry.getKey();
            tag.values().forEach((block) -> tagMapOut.put(block, id));
        }

        return tagMapOut;
    }
}
