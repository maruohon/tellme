package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ChunkProcessorEntityCounterPerType extends ChunkProcessorBase
{
    private final Object2IntOpenHashMap<EntityType<?>> perTypeCount = new Object2IntOpenHashMap<>();
    private int totalCount;

    public ChunkProcessorEntityCounterPerType(DataDump.Format format)
    {
        super(format);
    }

    @Override
    public void processChunk(WorldChunk chunk)
    {
        ChunkPos pos = chunk.getPos();
        int total = WorldUtils.countEntitiesInChunk(chunk.getWorld(), pos.x, pos.z, this.perTypeCount);

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
        List<EntitiesPerTypeHolder> counts = new ArrayList<>();

        for (Map.Entry<EntityType<?>, Integer> entry : this.perTypeCount.object2IntEntrySet())
        {
            counts.add(new EntitiesPerTypeHolder(entry.getKey(), entry.getValue()));
        }

        Collections.sort(counts);

        DataDump dump = new DataDump(2, this.format);

        dump.setSort(true).setSortColumn(1).setSortReverse(true);
        dump.addTitle("Entity type", "Count");

        final int loadedChunks = this.getLoadedChunkCount();
        final int zeroCount = this.getChunksWithZeroCount();

        dump.addHeader(String.format("The selected area contains %d loaded chunks", loadedChunks));
        dump.addHeader(String.format("and %d unloaded chunks.", this.getUnloadedChunkCount()));
        dump.addHeader("Loaded entities by entity type:");

        for (EntitiesPerTypeHolder holder : counts)
        {
            dump.addData(EntityInfo.getEntityNameFor(holder.type), String.valueOf(holder.count));
        }

        dump.addFooter(String.format("In total there were %d loaded entities in %d chunks.",
                this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        if (zeroCount != 0)
        {
            dump.addFooter(String.format("Out of %d loaded chunks in total,", loadedChunks));
            dump.addFooter(String.format("there were %d chunks with no entities.", zeroCount));
        }

        return dump;
    }
}
