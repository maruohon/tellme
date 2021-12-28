package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class BlockStatesDump
{
    public static List<String> getFormattedBlockStatesDumpByBlock()
    {
        List<String> outputLines = new ArrayList<>();

        for (Map.Entry<RegistryKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            Block block = entry.getValue();
            List<String> lines = new ArrayList<>();

            for (Entry<Property<?>, Comparable<?>> propertyComparableEntry : block.defaultBlockState().getValues().entrySet())
            {
                lines.add(propertyComparableEntry.getKey().toString());
            }

            outputLines.add(block.getRegistryName().toString() + ": " + String.join(", ", lines));
        }

        Collections.sort(outputLines);

        outputLines.add(0, "Block registry name | BlockState properties");
        outputLines.add(1, "-------------------------------------------------------------------------------------");

        return outputLines;
    }

    public static List<String> getFormattedBlockStatesDumpByState(DataDump.Format format)
    {
        DataDump blockStatesDump = new DataDump(2, format);

        for (Map.Entry<RegistryKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries())
        {
            Block block = entry.getValue();
            String regName = block.getRegistryName().toString();

            ImmutableList<BlockState> validStates = block.getStateDefinition().getPossibleStates();

            for (BlockState state : validStates)
            {
                List<String> lines = new ArrayList<>();

                for (Entry<Property<?>, Comparable<?>> propEntry : state.getValues().entrySet())
                {
                    lines.add(propEntry.getKey().getName() + "=" + propEntry.getValue().toString());
                }

                blockStatesDump.addData(regName, String.join(",", lines));
            }
        }

        blockStatesDump.addTitle("Block registry name", "BlockState properties");

        return blockStatesDump.getLines();
    }
}
