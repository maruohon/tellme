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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlockStatesDump extends DataDump
{
    protected BlockStatesDump(int columns)
    {
        super(columns);
    }

    public static List<String> getFormattedBlockStatesDumpByBlock()
    {
        List<String> outputLines = new ArrayList<String>();
        Iterator<Map.Entry<ResourceLocation, Block>> iter = ForgeRegistries.BLOCKS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Block> entry = iter.next();
            Block block = entry.getValue();

            List<String> lines = new ArrayList<String>();
            UnmodifiableIterator<Entry<IProperty<?>, Comparable<?>>> propIter = block.getDefaultState().getProperties().entrySet().iterator();

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

    public static List<String> getFormattedBlockStatesDumpByState()
    {
        BlockStatesDump blockStatesDump = new BlockStatesDump(2);
        Iterator<Map.Entry<ResourceLocation, Block>> iter = ForgeRegistries.BLOCKS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Block> entry = iter.next();
            Block block = entry.getValue();
            String regName = entry.getKey().toString();

            ImmutableList<IBlockState> validStates = block.getBlockState().getValidStates();

            for (IBlockState state : validStates)
            {
                List<String> lines = new ArrayList<String>();
                UnmodifiableIterator<Entry<IProperty<?>, Comparable<?>>> propIter = state.getProperties().entrySet().iterator();

                while (propIter.hasNext())
                {
                    Entry<IProperty<?>, Comparable<?>> propEntry = propIter.next();
                    lines.add(propEntry.getKey().getName() + "=" + propEntry.getValue().toString());
                }

                blockStatesDump.addData(regName, String.join(",", lines));
            }
        }

        blockStatesDump.addTitle("Block registry name", "BlockState properties");
        blockStatesDump.setUseColumnSeparator(true);
        //blockStatesDump.setSort(false);

        return blockStatesDump.getLines();
    }
}
