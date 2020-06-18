package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ChunkProcessorTileEntityCounterPerChunk extends ChunkProcessorBase
{
    private Map<ChunkPos, Integer> perChunkTotalCount = new HashMap<>();
    private Map<ChunkPos, Integer> perChunkTickingCount = new HashMap<>();
    private int totalCount;
    private int tickingCount;

    public ChunkProcessorTileEntityCounterPerChunk(DataDump.Format format)
    {
        super(format);
    }

    @Override
    public void processChunk(WorldChunk chunk)
    {
        ChunkPos pos = chunk.getPos();
        Map<BlockPos, BlockEntity> map = chunk.getBlockEntities();
        final int count = map.size();

        if (count > 0)
        {
            int tickingCount = 0;

            for (BlockEntity te : map.values())
            {
                if (te instanceof Tickable)
                {
                    ++tickingCount;
                }
            }

            this.perChunkTotalCount.put(pos, count);
            this.perChunkTickingCount.put(pos, tickingCount);
            this.totalCount += count;
            this.tickingCount += tickingCount;
        }
        else
        {
            ++this.chunksWithZeroCount;
        }
    }

    @Override
    public DataDump getDump()
    {
        List<TileEntityCountsPerChunkHolder> counts = new ArrayList<>();

        for (ChunkPos pos : this.perChunkTotalCount.keySet())
        {
            counts.add(new TileEntityCountsPerChunkHolder(pos, this.perChunkTotalCount.get(pos), this.perChunkTickingCount.get(pos)));
        }

        Collections.sort(counts);

        DataDump dump = new DataDump(4, this.format);

        dump.setSort(true).setSortReverse(true);
        dump.addHeader("Loaded TileEntities by chunk:");
        dump.addTitle("Total Count", "Ticking", "Chunk", "Region");

        for (TileEntityCountsPerChunkHolder holder : counts)
        {
            dump.addData(
                    String.valueOf(holder.count),
                    String.valueOf(holder.tickingCount),
                    String.format("[%5d, %5d]", holder.pos.x, holder.pos.z),
                    String.format("r.%d.%d", holder.pos.x >> 5, holder.pos.z >> 5));
        }

        dump.addFooter(String.format("In total there were %d loaded TileEntities", this.totalCount));
        dump.addFooter(String.format("in %d chunks, of which %d are ticking.",
                this.getLoadedChunkCount() - this.chunksWithZeroCount, this.tickingCount));

        return dump;
    }
}
