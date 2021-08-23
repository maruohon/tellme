package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ChunkProcessorEntityCounterPerChunk extends ChunkProcessorBase
{
    private final Map<ChunkPos, Integer> perChunkCount = new HashMap<>();
    private int totalCount;

    public ChunkProcessorEntityCounterPerChunk(DataDump.Format format)
    {
        super(format);
    }

    @Override
    public void processChunk(WorldChunk chunk)
    {
        ChunkPos pos = chunk.getPos();
        int total = WorldUtils.getEntityCountInChunk(chunk.getWorld(), pos.x, pos.z);

        if (total > 0)
        {
            this.perChunkCount.put(chunk.getPos(), total);
        }

        if (total > 0)
        {
            this.totalCount += total;
        }
        else
        {
            ++this.chunksWithZeroCount;
        }
    }

    @Override
    public DataDump getDump()
    {
        List<CountsPerChunkHolder> counts = new ArrayList<>();

        for (ChunkPos pos : this.perChunkCount.keySet())
        {
            counts.add(new CountsPerChunkHolder(pos, this.perChunkCount.get(pos)));
        }

        Collections.sort(counts);

        DataDump dump = new DataDump(3, this.format);

        dump.setSort(true).setSortReverse(true);
        dump.addTitle("Count", "Chunk", "Region");

        final int loadedChunks = this.getLoadedChunkCount();
        final int zeroCount = this.getChunksWithZeroCount();

        dump.addHeader(String.format("The selected area contains %d loaded chunks", loadedChunks));
        dump.addHeader(String.format("and %d unloaded chunks.", this.getUnloadedChunkCount()));
        dump.addHeader("Loaded entities by chunk:");

        for (CountsPerChunkHolder holder : counts)
        {
            dump.addData(
                    String.valueOf(holder.count),
                    String.format("[%5d, %5d]", holder.pos.x, holder.pos.z),
                    String.format("r.%d.%d", holder.pos.x >> 5, holder.pos.z >> 5));
        }

        dump.addFooter(String.format("In total there were %d loaded entities in %d chunks.",
                this.totalCount, this.getLoadedChunkCount() - zeroCount));

        if (zeroCount != 0)
        {
            dump.addFooter(String.format("Out of %d loaded chunks in total,", loadedChunks));
            dump.addFooter(String.format("there were %d chunks with no entities.", zeroCount));
        }

        return dump;
    }
}
