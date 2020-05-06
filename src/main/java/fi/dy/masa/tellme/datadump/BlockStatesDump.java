package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.util.datadump.DataDump;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockStatesDump
{
    public static List<String> getFormattedBlockStatesDumpByBlock()
    {
        List<String> outputLines = new ArrayList<String>();
        Iterator<Map.Entry<ResourceLocation, Block>> iter = ForgeRegistries.BLOCKS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Block> entry = iter.next();
            Block block = entry.getValue();

            List<String> lines = new ArrayList<String>();
            UnmodifiableIterator<Entry<IProperty<?>, Comparable<?>>> propIter = block.getDefaultState().getValues().entrySet().iterator();

            while (propIter.hasNext())
            {
                lines.add(propIter.next().getKey().toString());
            }

            outputLines.add(entry.getKey().toString() + ": " + String.join(", ", lines));
        }

        Collections.sort(outputLines);

        outputLines.add(0, "Block registry name | BlockState properties");
        outputLines.add(1, "-------------------------------------------------------------------------------------");

        return outputLines;
    }

    public static List<String> getFormattedBlockStatesDumpByState(DataDump.Format format)
    {
        DataDump blockStatesDump = new DataDump(2, format);
        Iterator<Map.Entry<ResourceLocation, Block>> iter = ForgeRegistries.BLOCKS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Block> entry = iter.next();
            Block block = entry.getValue();
            String regName = entry.getKey().toString();

            ImmutableList<BlockState> validStates = block.getStateContainer().getValidStates();

            for (BlockState state : validStates)
            {
                List<String> lines = new ArrayList<String>();
                UnmodifiableIterator<Entry<IProperty<?>, Comparable<?>>> propIter = state.getValues().entrySet().iterator();

                while (propIter.hasNext())
                {
                    Entry<IProperty<?>, Comparable<?>> propEntry = propIter.next();
                    lines.add(propEntry.getKey().getName() + "=" + propEntry.getValue().toString());
                }

                blockStatesDump.addData(regName, String.join(",", lines));
            }
        }

        blockStatesDump.addTitle("Block registry name", "BlockState properties");

        return blockStatesDump.getLines();
    }
}
