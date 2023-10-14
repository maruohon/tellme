package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

import fi.dy.masa.tellme.util.datadump.DataDump;

public class BlockStatesDump
{
    public static List<String> getFormattedBlockStatesDumpByBlock()
    {
        List<String> outputLines = new ArrayList<>();

        for (Identifier id : Registries.BLOCK.getIds())
        {
            Block block = Registries.BLOCK.get(id);

            List<String> lines = new ArrayList<>();
            UnmodifiableIterator<Entry<Property<?>, Comparable<?>>> propIter = block.getDefaultState().getEntries().entrySet().iterator();

            while (propIter.hasNext())
            {
                lines.add(propIter.next().getKey().toString());
            }

            outputLines.add(id.toString() + ": " + String.join(", ", lines));
        }

        Collections.sort(outputLines);

        outputLines.add(0, "Block registry name | BlockState properties");
        outputLines.add(1, "-------------------------------------------------------------------------------------");

        return outputLines;
    }

    public static List<String> getFormattedBlockStatesDumpByState(DataDump.Format format)
    {
        DataDump blockStatesDump = new DataDump(2, format);

        for (Identifier id : Registries.BLOCK.getIds())
        {
            Block block = Registries.BLOCK.get(id);
            String regName = id.toString();

            ImmutableList<BlockState> validStates = block.getStateManager().getStates();

            for (BlockState state : validStates)
            {
                List<String> lines = new ArrayList<String>();
                UnmodifiableIterator<Entry<Property<?>, Comparable<?>>> propIter = state.getEntries().entrySet().iterator();

                while (propIter.hasNext())
                {
                    Entry<Property<?>, Comparable<?>> propEntry = propIter.next();
                    lines.add(propEntry.getKey().getName() + "=" + propEntry.getValue().toString());
                }

                blockStatesDump.addData(regName, String.join(",", lines));
            }
        }

        blockStatesDump.addTitle("Block registry name", "BlockState properties");

        return blockStatesDump.getLines();
    }
}
