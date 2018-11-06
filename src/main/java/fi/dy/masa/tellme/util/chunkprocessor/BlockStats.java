package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class BlockStats extends ChunkProcessorAllChunks
{
    private boolean append;
    private final Multimap<String, BlockInfo> blockStats = MultimapBuilder.hashKeys().arrayListValues().build();

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    @Override
    public void processChunks(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax)
    {
        Object2LongOpenHashMap<IBlockState> counts = new Object2LongOpenHashMap<IBlockState>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        final long timeBefore = System.currentTimeMillis();
        int count = 0;

        for (Chunk chunk : chunks)
        {
            final int topY = chunk.getTopFilledSegment() + 15;
            final int xMin = Math.max(chunk.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunk.z << 4, posMin.getZ());
            final int xMax = Math.min((chunk.x << 4) + 15, posMax.getX());
            final int yMax = Math.min(topY, posMax.getY());
            final int zMax = Math.min((chunk.z << 4) + 15, posMax.getZ());

            for (int z = zMin; z <= zMax; ++z)
            {
                for (int x = xMin; x <= xMax; ++x)
                {
                    for (int y = yMin; y <= yMax; ++y)
                    {
                        pos.setPos(x, y, z);
                        IBlockState state = chunk.getBlockState(pos);

                        counts.addTo(state, 1);
                        count++;
                    }
                }
            }

            // Add the amount of air that would be in non-existing chunk sections within the given volume
            if (topY < posMax.getY())
            {
                counts.addTo(Blocks.AIR.getDefaultState(), (posMax.getY() - topY) * 256);
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.logger.info(String.format(Locale.US, "Counted %d blocks in %d chunks in %.3f seconds.",
                count, chunks.size(), (timeAfter - timeBefore) / 1000f));

        this.addParsedData(counts);
    }

    private void addParsedData(Object2LongOpenHashMap<IBlockState> counts)
    {
        if (this.append == false)
        {
            this.blockStats.clear();
        }

        for (IBlockState state : counts.keySet())
        {
            try
            {
                Block block = state.getBlock();
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
                String registryName = key != null ? key.toString() : "null";
                int id = Block.getIdFromBlock(block);
                int meta = block.getMetaFromState(state);
                ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));
                String displayName = stack.isEmpty() == false ? stack.getDisplayName() : registryName;
                long amount = counts.getLong(state);

                if (key == null)
                {
                    TellMe.logger.warn("Non-registered block: class = {}, id = {}, meta = {}, state 0 {}",
                            block.getClass().getName(), id, meta, state);
                }

                if (this.append)
                {
                    boolean appended = false;

                    for (BlockInfo old : this.blockStats.get(registryName))
                    {
                        if (old.id == id && old.meta == meta)
                        {
                            old.addToCount(amount);
                            appended = true;
                            break;
                        }
                    }

                    if (appended == false)
                    {
                        this.blockStats.put(registryName, new BlockInfo(registryName, displayName, id, meta, amount));
                    }
                }
                else
                {
                    this.blockStats.put(registryName, new BlockInfo(registryName, displayName, id, meta, amount));
                }
            }
            catch (Exception e)
            {
                TellMe.logger.error("Caught an exception while getting block names", e);
            }
        }
    }

    private List<BlockInfo> getFilteredData(DataDump dump, List<String> filters, boolean sortByCount)
    {
        List<BlockInfo> list = new ArrayList<>();

        for (String filter : filters)
        {
            int firstSemi = filter.indexOf(":");

            if (firstSemi == -1)
            {
                filter = "minecraft:" + filter;
            }

            int lastSemi = filter.lastIndexOf(":");

            // At least two ':' characters found; assume the first separates the modid and block name,
            // and the second separates the block name and meta.
            if (lastSemi != firstSemi && lastSemi < (filter.length() - 1))
            {
                try
                {
                    int meta = Integer.parseInt(filter.substring(lastSemi + 1, filter.length()));

                    for (BlockInfo info : this.blockStats.get(filter))
                    {
                        if (info.meta == meta)
                        {
                            list.add(info);
                            break;
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    TellMe.logger.error("Caught an exception while parsing block meta value from user input", e);
                }
            }
            else
            {
                for (BlockInfo info : this.blockStats.get(filter))
                {
                    list.add(info);
                }
            }
        }

        return list;
    }

    public List<String> queryAll(Format format, boolean sortByCount)
    {
        return this.query(format, null, sortByCount);
    }

    public List<String> query(Format format, @Nullable List<String> filters, boolean sortByCount)
    {
        DataDump dump = new DataDump(5, format);
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
            dump.addData(info.registryName, String.valueOf(info.id), String.valueOf(info.meta), info.displayName, String.valueOf(info.count));
        }

        dump.addTitle("Registry name", "ID", "meta", "Display name", "Count");
        dump.addHeader("NOTE: The Block ID is for very specific low-level purposes only!");
        dump.addHeader("It WILL be different in every world since Minecraft 1.7,");
        dump.addHeader("because they are dynamically allocated by the game!");

        dump.setColumnProperties(1, Alignment.RIGHT, true); // Block ID
        dump.setColumnProperties(2, Alignment.RIGHT, true); // meta
        dump.setColumnProperties(4, Alignment.RIGHT, true); // count

        dump.setUseColumnSeparator(true);
        dump.setSort(sortByCount == false);

        return dump.getLines();
    }

    private static class BlockInfo implements Comparable<BlockInfo>
    {
        private static boolean sortByCount = false;
        public final String registryName;
        public final String displayName;
        public final int id;
        public final int meta;
        public long count;

        public BlockInfo(String name, String displayName, int id, int meta, long count)
        {
            this.registryName = name;
            this.displayName = displayName;
            this.id = id;
            this.meta = meta;
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
                int val = this.registryName.compareTo(other.registryName);

                if (val != 0)
                {
                    return val;
                }

                if (this.id != other.id)
                {
                    return this.id - other.id;
                }

                if (this.meta != other.meta)
                {
                    return this.meta - other.meta;
                }
            }

            return 0;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + meta;
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
            if (id != other.id)
                return false;
            if (meta != other.meta)
                return false;
            return true;
        }
    }
}
