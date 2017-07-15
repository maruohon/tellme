package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.BiomeDump.IdToStringHolder;
import fi.dy.masa.tellme.util.ModNameUtils;

public class BlockDump extends DataDump
{
    private boolean dumpNBT;

    private BlockDump(Format format, boolean dumpNBT)
    {
        super(dumpNBT ? 10 : 9, format);

        this.dumpNBT = dumpNBT;
    }

    protected List<String> getLines()
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
        String displayName = stack.isEmpty() == false ? stack.getDisplayName() : block.getLocalizedName();
        Item item = Item.getItemFromBlock(block);
        String itemId = item != Items.AIR ? String.valueOf(Item.getIdFromItem(item)) : EMPTY_STRING;
        String itemMeta = stack.isEmpty() == false ? String.valueOf(stack.getMetadata()) : EMPTY_STRING;
        String subTypes = subTypesKnown ? String.valueOf(hasSubTypes) : "?";
        String exists = isDummied(ForgeRegistries.BLOCKS, rl) ? "false" : "true";

        if (this.dumpNBT)
        {
            String nbt = stack.isEmpty() == false && stack.getTagCompound() != null ? stack.getTagCompound().toString() : EMPTY_STRING;
            this.addData(modName, registryName, blockId, subTypes, itemId, itemMeta, displayName, exists, ItemDump.getOredictKeysJoined(stack), nbt);
        }
        else
        {
            this.addData(modName, registryName, blockId, subTypes, itemId, itemMeta, displayName, exists, ItemDump.getOredictKeysJoined(stack));
        }
    }

    public static List<String> getFormattedBlockDump(Format format, boolean dumpNBT)
    {
        BlockDump blockDump = new BlockDump(format, dumpNBT);
        Iterator<Map.Entry<ResourceLocation, Block>> iter = ForgeRegistries.BLOCKS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Block> entry = iter.next();
            TellMe.proxy.getDataForBlockSubtypes(entry.getValue(), entry.getKey(), blockDump);
        }

        if (dumpNBT)
        {
            blockDump.addTitle("Mod name", "Registry name", "BlockID", "Subtypes", "Item ID", "Item meta", "Display name", "Exists", "Ore Dict keys", "NBT");
        }
        else
        {
            blockDump.addTitle("Mod name", "Registry name", "BlockID", "Subtypes", "Item ID", "Item meta", "Display name", "Exists", "Ore Dict keys");
        }

        blockDump.setColumnProperties(2, Alignment.RIGHT, true); // ID
        blockDump.setColumnAlignment(3, Alignment.RIGHT); // sub-types
        blockDump.setColumnProperties(4, Alignment.RIGHT, true); // item id
        blockDump.setColumnProperties(5, Alignment.RIGHT, true); // item meta

        blockDump.setUseColumnSeparator(true);

        return blockDump.getLines();
    }

    public static List<String> getBlockDumpIdToRegistryName(Format format)
    {
        List<IdToStringHolder> data = new ArrayList<IdToStringHolder>();
        List<String> lines = new ArrayList<String>();
        Iterator<Block> iter = Block.REGISTRY.iterator();

        while (iter.hasNext())
        {
            Block block = iter.next();

            if (block != null && block.getRegistryName() != null)
            {
                data.add(new IdToStringHolder(Block.getIdFromBlock(block), block.getRegistryName().toString()));
            }
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
}
