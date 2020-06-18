package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockArgumentParser;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class BlockStats extends ChunkProcessorAllChunks
{
    private final HashMap<BlockState, BlockInfo> blockStats = new HashMap<>();
    private int chunkCount;
    private boolean append;

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    @Override
    public void processChunks(Collection<WorldChunk> chunks, BlockPos pos1, BlockPos pos2)
    {
        final long timeBefore = System.nanoTime();
        Object2LongOpenHashMap<BlockState> counts = new Object2LongOpenHashMap<>();
        BlockPos.Mutable pos = new BlockPos.Mutable(0, 0, 0);
        final BlockState air = Blocks.AIR.getDefaultState();
        BlockPos posMin = getMinCorner(pos1, pos2);
        BlockPos posMax = getMaxCorner(pos1, pos2);
        int count = 0;

        for (Chunk chunk : chunks)
        {
            ChunkPos chunkPos = chunk.getPos();
            final int topY = chunk.getHighestNonEmptySectionYOffset() + 15;
            final int xMin = Math.max(chunkPos.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunkPos.z << 4, posMin.getZ());
            final int xMax = Math.min((chunkPos.x << 4) + 15, posMax.getX());
            final int yMax = Math.min(topY, posMax.getY());
            final int zMax = Math.min((chunkPos.z << 4) + 15, posMax.getZ());

            for (int z = zMin; z <= zMax; ++z)
            {
                for (int x = xMin; x <= xMax; ++x)
                {
                    for (int y = yMin; y <= yMax; ++y)
                    {
                        pos.set(x, y, z);
                        BlockState state = chunk.getBlockState(pos);

                        counts.addTo(state, 1);
                        count++;
                    }
                }
            }

            // Add the amount of air that would be in non-existing chunk sections within the given volume
            if (topY < posMax.getY())
            {
                counts.addTo(air, (posMax.getY() - topY) * 256);
            }
        }

        this.chunkCount = this.append ? this.chunkCount + chunks.size() : chunks.size();

        TellMe.logger.info(String.format(Locale.US, "Counted %d blocks in %d chunks in %.4f seconds.",
                count, chunks.size(), (System.nanoTime() - timeBefore) / 1000000000D));

        this.addParsedData(counts);
    }

    private void addParsedData(Object2LongOpenHashMap<BlockState> counts)
    {
        if (this.append == false)
        {
            this.blockStats.clear();
        }

        for (BlockState state : counts.keySet())
        {
            try
            {
                Block block = state.getBlock();
                Identifier key = Registry.BLOCK.getId(block);
                String registryName = key != null ? key.toString() : "<null>";
                ItemStack stack = new ItemStack(block);
                String displayName = stack.isEmpty() == false ? stack.getName().getString() : (new TranslatableText(block.getTranslationKey())).getString();
                long amount = counts.getLong(state);

                if (key == null)
                {
                    TellMe.logger.warn("Non-registered block: class = {}, state = {}", block.getClass().getName(), state);
                }

                BlockInfo info = this.blockStats.computeIfAbsent(state, (s) -> new BlockInfo(state, registryName, displayName, 0));

                if (this.append)
                {
                    info.addToCount(amount);
                }
                else
                {
                    info.setCount(amount);
                }
            }
            catch (Exception e)
            {
                TellMe.logger.error("Caught an exception while getting block names", e);
            }
        }
    }

    private List<BlockInfo> getFilteredData(DataDump dump, List<String> filters, boolean sortByCount) throws CommandSyntaxException
    {
        ArrayList<BlockInfo> list = new ArrayList<>();
        ArrayListMultimap<Block, BlockInfo> infoByBlock = ArrayListMultimap.create();

        for (BlockInfo info : this.blockStats.values())
        {
            infoByBlock.put(info.state.getBlock(), info);
        }

        for (String filter : filters)
        {
            StringReader reader = new StringReader(filter);
            BlockArgumentParser parser = (new BlockArgumentParser(reader, false)).parse(false);
            BlockState state = parser.getBlockState();
            Block block = state.getBlock();

            // No block state properties specified, get all states for this block
            if (parser.getProperties().size() == 0)
            {
                list.addAll(infoByBlock.get(block));
            }
            // Exact state specified, only add that
            else if (parser.getProperties().size() == state.getProperties().size())
            {
                BlockInfo info = this.blockStats.get(state);

                if (info != null)
                {
                    list.add(info);
                }
            }
            // Some properties specified, filter by those
            else
            {
                // TODO 1.14+
            }
        }

        return list;
    }

    public List<String> queryAll(Format format, boolean sortByCount) throws CommandSyntaxException
    {
        return this.query(format, null, sortByCount);
    }

    public List<String> query(Format format, @Nullable List<String> filters, boolean sortByCount) throws CommandSyntaxException
    {
        DataDump dump = new DataDump(3, format);
        List<BlockInfo> list = new ArrayList<>();

        if (filters != null)
        {
            list.addAll(this.getFilteredData(dump, filters, sortByCount));
        }
        else
        {
            list.addAll(this.blockStats.values());
        }

        BlockInfo.setSortByCount(sortByCount);
        Collections.sort(list);

        for (BlockInfo info : list)
        {
            dump.addData(info.registryName, info.displayName, String.valueOf(info.count));
        }

        dump.addTitle("Registry name", "Display name", "Count");
        dump.addFooter(String.format("Block stats from an area touching %d chunks", this.chunkCount));

        dump.setColumnProperties(2, Alignment.RIGHT, true); // count
        dump.setSort(sortByCount == false);

        return dump.getLines();
    }

    private static class BlockInfo implements Comparable<BlockInfo>
    {
        private static boolean sortByCount = false;
        public final BlockState state;
        public final String registryName;
        public final String displayName;
        public long count;

        public BlockInfo(BlockState state, String name, String displayName, long count)
        {
            this.state = state;
            this.registryName = name;
            this.displayName = displayName;
            this.count = count;
        }

        public static void setSortByCount(boolean sortByCount)
        {
            BlockInfo.sortByCount = sortByCount;
        }

        public void addToCount(long amount)
        {
            this.count += amount;
        }

        public void setCount(long amount)
        {
            this.count = amount;
        }

        public int compareTo(BlockInfo other)
        {
            if (other == null)
            {
                throw new NullPointerException();
            }

            if (sortByCount)
            {
                return this.count > other.count ? -1 : (this.count < other.count ? 1 : this.registryName.compareTo(other.registryName));
            }
            else
            {
                return this.registryName.compareTo(other.registryName);
            }
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((registryName == null) ? 0 : registryName.hashCode());
            result = prime * result + ((state == null) ? 0 : state.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BlockInfo other = (BlockInfo) obj;
            if (registryName == null)
            {
                if (other.registryName != null)
                    return false;
            }
            else if (!registryName.equals(other.registryName))
                return false;
            if (state == null)
            {
                if (other.state != null)
                    return false;
            }
            else if (!state.equals(other.state))
                return false;
            return true;
        }
    }
}
