package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ChunkProcessorBlockEntityCounterPerChunk extends ChunkProcessorBase
{
    private final Map<ChunkPos, Integer> perChunkTotalCount = new HashMap<>();
    private int totalCount;

    public ChunkProcessorBlockEntityCounterPerChunk(DataDump.Format format)
    {
        super(format);
    }

    @Override
    public void processChunk(LevelChunk chunk)
    {
        ChunkPos pos = chunk.getPos();
        final int count = chunk.getBlockEntities().size();

        if (count > 0)
        {
            this.perChunkTotalCount.put(pos, count);
            this.totalCount += count;
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

        for (ChunkPos pos : this.perChunkTotalCount.keySet())
        {
            counts.add(new CountsPerChunkHolder(pos, this.perChunkTotalCount.get(pos)));
        }

        Collections.sort(counts);

        DataDump dump = new DataDump(3, this.format);

        dump.setSort(true).setSortReverse(true);
        dump.addHeader("Loaded BlockEntities by chunk:");
        dump.addTitle("Total Count", "Chunk", "Region");

        for (CountsPerChunkHolder holder : counts)
        {
            dump.addData(
                    String.valueOf(holder.count),
                    String.format("[%5d, %5d]", holder.pos.x, holder.pos.z),
                    String.format("r.%d.%d", holder.pos.x >> 5, holder.pos.z >> 5));
        }

        dump.addFooter(String.format("In total there were %d loaded BlockEntities in %d chunks",
                                     this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        return dump;
    }
}
