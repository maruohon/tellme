package fi.dy.masa.tellme.util;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;

public class BlockStats extends ChunkProcessor
{
    private final Multimap<String, BlockInfo> blockStats = MultimapBuilder.hashKeys().arrayListValues().build();

    @Override
    public void processChunks(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax)
    {
        @SuppressWarnings("deprecation")
        final int size = Math.max(Block.BLOCK_STATE_IDS.size(), 65536);
        final int[] counts = new int[size];
        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        final long timeBefore = System.currentTimeMillis();

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

                        @SuppressWarnings("deprecation")
                        int id = Block.BLOCK_STATE_IDS.get(state);
                        counts[id]++;
                        count++;
                    }
                }
            }

            // Add the amount of air that would be in non-existing chunk sections within the given volume
            if (topY < posMax.getY())
            {
                @SuppressWarnings("deprecation")
                int id = Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());
                counts[id] += (posMax.getY() - topY) * 256;
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.logger.info(String.format(Locale.US, "Counted %d blocks in %d chunks in %.3f seconds.",
                count, chunks.size(), (timeAfter - timeBefore) / 1000f));

        this.addParsedData(counts);
    }

    private void addParsedData(final int[] counts)
    {
        this.blockStats.clear();

        for (int i = 0; i < counts.length; ++i)
        {
            if (counts[i] > 0)
            {
                try
                {
                    @SuppressWarnings("deprecation")
                    IBlockState state = Block.BLOCK_STATE_IDS.getByValue(i);
                    Block block = state.getBlock();
                    String registryName = ForgeRegistries.BLOCKS.getKey(block).toString();
                    int id = Block.getIdFromBlock(block);
                    int meta = block.getMetaFromState(state);
                    ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));
                    String displayName = stack.isEmpty() == false ? stack.getDisplayName() : registryName;

                    this.blockStats.put(registryName, new BlockInfo(registryName, displayName, id, meta, counts[i]));
                }
                catch (Exception e)
                {
                    TellMe.logger.error("Caught an exception while getting block names", e);
                }
            }
        }
    }

    private void addFilteredData(BlockStatsDump dump, List<String> filters)
    {
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
                            dump.addData(info.name, String.valueOf(info.id), String.valueOf(info.meta), info.displayName, String.valueOf(info.count));
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
                    dump.addData(info.name, String.valueOf(info.id), String.valueOf(info.meta), info.displayName, String.valueOf(info.count));
                }
            }
        }
    }

    public List<String> queryAll(Format format)
    {
        return this.query(format, null);
    }

    public List<String> query(Format format, @Nullable List<String> filters)
    {
        BlockStatsDump dump = new BlockStatsDump(format);

        if (filters != null)
        {
            this.addFilteredData(dump, filters);
        }
        else
        {
            for (BlockInfo info : this.blockStats.values())
            {
                dump.addData(info.name, String.valueOf(info.id), String.valueOf(info.meta), info.displayName, String.valueOf(info.count));
            }
        }

        dump.addTitle("Registry name", "ID", "meta", "Display name", "Count");
        dump.addHeader("NOTE: The Block ID is for very specific low-level purposes only!");
        dump.addHeader("It WILL be different in every world since Minecraft 1.7,");
        dump.addHeader("because they are dynamically allocated by the game!");

        dump.setColumnProperties(1, Alignment.RIGHT, true); // Block ID
        dump.setColumnProperties(2, Alignment.RIGHT, true); // meta
        dump.setColumnProperties(4, Alignment.RIGHT, true); // count

        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }

    private static class BlockInfo implements Comparable<BlockInfo>
    {
        public final String name;
        public final String displayName;
        public final int id;
        public final int meta;
        public final int count;

        public BlockInfo(String name, String displayName, int id, int meta, int count)
        {
            this.name = name;
            this.displayName = displayName;
            this.id = id;
            this.meta = meta;
            this.count = count;
        }

        public int compareTo(BlockInfo other)
        {
            if (other == null)
            {
                throw new NullPointerException();
            }

            if (this.id != other.id)
            {
                return this.id - other.id;
            }

            if (this.meta != other.meta)
            {
                return this.meta - other.meta;
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

    private static class BlockStatsDump extends DataDump
    {
        public BlockStatsDump(Format format)
        {
            super(5, format);
        }
    }
}
