package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.datadump.EntityCountDump;

public class EntitiesPerChunkCounter extends ChunkProcessorLoadedChunks
{
    private Map<ChunkPos, Integer> perChunkCount = new HashMap<ChunkPos, Integer>();
    private int totalCount;

    @Override
    public void processChunk(Chunk chunk)
    {
        ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
        int total = 0;

        for (int i = 0; i < entityLists.length; i++)
        {
            total += entityLists[i].size();
        }

        if (total == 0)
        {
            this.chunksWithZeroCount++;
        }
        else
        {
            this.perChunkCount.put(chunk.getPos(), total);
            this.totalCount += total;
        }
    }

    @Override
    public EntityCountDump createDump(World world)
    {
        List<CountsPerChunkHolder> counts = new ArrayList<CountsPerChunkHolder>();

        for (ChunkPos pos : this.perChunkCount.keySet())
        {
            counts.add(new CountsPerChunkHolder(pos, this.perChunkCount.get(pos)));
        }

        Collections.sort(counts);

        EntityCountDump dump = new EntityCountDump(3);
        dump.addTitle("Count", "Chunk", "Region");
        dump.addHeader("Loaded entities by chunk:");

        for (CountsPerChunkHolder holder : counts)
        {
            dump.addData(
                    String.valueOf(holder.count),
                    String.format("[%5d, %5d]", holder.pos.x, holder.pos.z),
                    String.format("r.%d.%d", holder.pos.x >> 5, holder.pos.z >> 5));
        }

        dump.addFooter(String.format("In total there were %d loaded entities in %d chunks.",
                this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        return dump;
    }
}
