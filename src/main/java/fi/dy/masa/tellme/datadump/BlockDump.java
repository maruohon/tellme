package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import fi.dy.masa.malilib.util.ItemUtils;
import fi.dy.masa.tellme.datadump.BiomeDump.IdToStringHolder;
import fi.dy.masa.tellme.mixin.IMixinBlock;
import fi.dy.masa.tellme.util.ModNameUtils;

public class BlockDump extends DataDump
{
    private boolean dumpNBT;

    private BlockDump(Format format, boolean dumpNBT)
    {
        super(dumpNBT ? 8 : 7, format);

        this.dumpNBT = dumpNBT;
    }

    @Override
    public List<String> getLines()
    {
        if (this.getFormat() != Format.ASCII)
        {
            return super.getLines();
        }

        List<String> lines = new ArrayList<String>();

        this.generateFormatStrings();

        lines.add(this.lineSeparator);
        lines.add("*** WARNING ***");
        lines.add("The block and item IDs are dynamic and will be different on each world!");
        lines.add("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
        lines.add("*** ALSO ***");
        lines.add("The server doesn't have a list of sub block and sub items");
        lines.add("(= items with different damage value or blocks with different metadata).");
        lines.add("That is why the block and item list dumps only contain one entry per block/item class (separate ID) when run on a server.");
        lines.add("NOTE: The metadata value displayed is from the ItemStacks from getSubBlocks(), it's NOT necessarily the metadata value in world!!");
        lines.add("NOTE: For blocks, Subtypes = true is only based on the number of returned ItemStacks from getSubBlocks() being > 1");
        lines.add("NOTE: For blocks, Subtypes = ? means that Item.getItemFromBlock(block) returned null or the command was run on the server side");
        lines.add("The Exists column indicates whether the block currently exists in the game,");
        lines.add("or is just a placeholder dummy air block for a remove block read from the block ID map in the level.dat file");

        // Get the actual data
        this.getFormattedData(lines);

        return lines;
    }

    public void addData(Block block, ResourceLocation rl, boolean subTypesKnown, boolean hasSubTypes, @Nonnull ItemStack stack)
    {
        String blockId = String.valueOf(Block.getIdFromBlock(block));
        String modName = ModNameUtils.getModName(rl);
        String registryName = rl.toString();
        boolean notEmpty = ItemUtils.notEmpty(stack);
        String displayName = notEmpty ? stack.getDisplayName() : block.getLocalizedName();
        displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);
        Item item = Item.getItemFromBlock(block);
        String itemId = item != Items.AIR ? String.valueOf(Item.getIdFromItem(item)) : EMPTY_STRING;
        String itemMeta = notEmpty ? String.valueOf(stack.getMetadata()) : EMPTY_STRING;
        String subTypes = subTypesKnown ? String.valueOf(hasSubTypes) : "?";

        if (this.dumpNBT)
        {
            NBTTagCompound tag = ItemUtils.getTag(stack);
            String nbt = notEmpty && tag != null ? tag.toString() : EMPTY_STRING;
            this.addData(modName, registryName, blockId, subTypes, itemId, itemMeta, displayName, nbt);
        }
        else
        {
            this.addData(modName, registryName, blockId, subTypes, itemId, itemMeta, displayName);
        }
    }

    public static List<String> getFormattedBlockDump(Format format, boolean dumpNBT)
    {
        BlockDump blockDump = new BlockDump(format, dumpNBT);

        for (ResourceLocation key : Block.REGISTRY.getKeys())
        {
            Block block = Block.REGISTRY.getObject(key);
            getDataForBlockSubtypes(block, key, blockDump);
        }

        if (dumpNBT)
        {
            blockDump.addTitle("Mod name", "Registry name", "BlockID", "Subtypes", "Item ID", "Item meta", "Display name", "NBT");
        }
        else
        {
            blockDump.addTitle("Mod name", "Registry name", "BlockID", "Subtypes", "Item ID", "Item meta", "Display name");
        }

        blockDump.setColumnProperties(2, Alignment.RIGHT, true); // ID
        blockDump.setColumnAlignment(3, Alignment.RIGHT); // sub-types
        blockDump.setColumnProperties(4, Alignment.RIGHT, true); // item id
        blockDump.setColumnProperties(5, Alignment.RIGHT, true); // item meta

        blockDump.setUseColumnSeparator(true);

        return blockDump.getLines();
    }

    public static List<String> getFormattedBlockPropertiesDump(Format format)
    {
        DataDump blockDump = new DataDump(4, format);

        for (ResourceLocation key : Block.REGISTRY.getKeys())
        {
            String modName = ModNameUtils.getModName(key);
            String registryName = key.toString();
            Block block = Block.REGISTRY.getObject(key);
            String hardness = String.format("%.2f", ((IMixinBlock) block).getBlockHardness());
            String resistance = String.format("%.2f", ((IMixinBlock) block).getBlockResistance());
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

        blockDump.setUseColumnSeparator(true);

        return blockDump.getLines();
    }

    public static List<String> getBlockDumpIdToRegistryName(Format format)
    {
        List<IdToStringHolder> data = new ArrayList<IdToStringHolder>();
        List<String> lines = new ArrayList<String>();

        for (ResourceLocation key : Block.REGISTRY.getKeys())
        {
            Block block = Block.REGISTRY.getObject(key);
            data.add(new IdToStringHolder(Block.getIdFromBlock(block), key.toString()));
        }

        Collections.sort(data);

        if (format == Format.ASCII)
        {
            for (IdToStringHolder holder : data)
            {
                lines.add(String.valueOf(holder.getId()) + " = " + holder.getString());
            }
        }
        else if (format == Format.CSV)
        {
            for (IdToStringHolder holder : data)
            {
                lines.add(String.valueOf(holder.getId()) + ",\"" + holder.getString() + "\"");
            }
        }

        return lines;
    }

    public static void getDataForBlockSubtypes(Block block, ResourceLocation rl, BlockDump blockDump)
    {
        NonNullList<ItemStack> stacks = NonNullList.<ItemStack>create();
        CreativeTabs tab = block.getCreativeTab();
        block.getSubBlocks(tab, stacks);

        if (stacks.size() > 0)
        {
            boolean hasSubTypes = stacks.size() > 1;

            for (ItemStack stack : stacks)
            {
                blockDump.addData(block, rl, true, hasSubTypes, stack);
            }
        }
        else
        {
            blockDump.addData(block, rl, false, false, ItemStack.EMPTY);
        }
    }

    public static String getJsonBlockDump()
    {
        HashMultimap<String, ResourceLocation> map = HashMultimap.create();

        // Get a mapping of modName => collection-of-block-names
        for (ResourceLocation key : Block.REGISTRY.getKeys())
        {
            map.put(key.getNamespace(), key);
        }

        // First sort by mod name
        List<String> mods = Lists.newArrayList(map.keySet());
        Collections.sort(mods);
        JsonObject root = new JsonObject();
        BlockDumpJsonDummy dump = new BlockDumpJsonDummy();

        for (String mod : mods)
        {
            // For each mod, sort the blocks by their registry name
            List<ResourceLocation> blocks = Lists.newArrayList(map.get(mod));
            Collections.sort(blocks);
            JsonObject objectMod = new JsonObject();

            for (ResourceLocation key : blocks)
            {
                JsonObject objBlock = new JsonObject();
                dump.setCurrentBlockObject(objBlock);
                getDataForBlockSubtypes(Block.REGISTRY.getObject(key), key, dump);
                objectMod.add(key.toString(), objBlock);
            }

            root.add(ModNameUtils.getModName(blocks.get(0)), objectMod);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(root);
    }

    // Dummy/fake BlockDump child, just to be able to use the same getDataForBlockSubtypes() method
    private static class BlockDumpJsonDummy extends BlockDump
    {
        private JsonObject obj;
        private boolean firstEntry;

        public BlockDumpJsonDummy()
        {
            super(Format.CSV, false);
        }

        public void setCurrentBlockObject(JsonObject obj)
        {
            this.obj = obj;
            this.firstEntry = true;
        }

        @Override
        public void addData(Block block, ResourceLocation rl, boolean subTypesKnown, boolean hasSubTypes, @Nonnull ItemStack stack)
        {
            if (this.firstEntry)
            {
                int blockId = Block.getIdFromBlock(block);
                String subTypes = subTypesKnown ? String.valueOf(hasSubTypes) : "?";

                this.obj.add("BlockID", new JsonPrimitive(blockId));
                this.obj.add("SubTypes", new JsonPrimitive(subTypes));
            }

            Item item = stack.getItem();

            if (item != Items.AIR)
            {
                String itemName = Item.REGISTRY.getNameForObject(item).toString();
                int itemId = Item.getIdFromItem(item);
                int itemMeta = stack.getMetadata();
                String displayName = ItemUtils.notEmpty(stack) ? stack.getDisplayName() : block.getLocalizedName();
                displayName = TextFormatting.getTextWithoutFormattingCodes(displayName);

                JsonObject objItem = new JsonObject();
                objItem.add("RegistryName", new JsonPrimitive(itemName));
                objItem.add("ItemID", new JsonPrimitive(itemId));
                objItem.add("ItemMeta", new JsonPrimitive(itemMeta));
                objItem.add("DisplayName", new JsonPrimitive(displayName));

                if (hasSubTypes)
                {
                    JsonArray arr;

                    if (this.firstEntry)
                    {
                        arr = new JsonArray();
                        this.obj.add("Item", arr);
                    }
                    // Second or later ItemStack entry - hasSubTypes will be true here, and the array will already exist
                    else
                    {
                        arr = this.obj.get("Item").getAsJsonArray();
                    }

                    arr.add(objItem);
                }
                else
                {
                    this.obj.add("Item", objItem);
                }
            }

            this.firstEntry = false;
        }
    }
}
