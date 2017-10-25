package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.datadump.EntityCountDump;

public class TileEntitiesPerChunkCounter extends ChunkProcessor
{
    private Map<ChunkPos, Integer> perChunkTotalCount = new HashMap<ChunkPos, Integer>();
    private Map<ChunkPos, Integer> perChunkTickingCount = new HashMap<ChunkPos, Integer>();
    private int totalCount;
    private int tickingCount;

    @Override
    public void processChunk(Chunk chunk)
    {
        Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
        ChunkPos pos = chunk.getPos();
        int count = chunk.getTileEntityMap().size();

        if (count == 0)
        {
            this.chunksWithZeroCount++;
        }
        else
        {
            int tickingCount = 0;

            for (TileEntity te : map.values())
            {
                if (te instanceof ITickable)
                {
                    tickingCount++;
                }
            }

            this.perChunkTotalCount.put(pos, count);
            this.perChunkTickingCount.put(pos, tickingCount);
            this.totalCount += count;
            this.tickingCount += tickingCount;
        }
    }

    @Override
    public EntityCountDump createDump(World world)
    {
        List<TileEntityCountsPerChunkHolder> counts = new ArrayList<TileEntityCountsPerChunkHolder>();

        for (ChunkPos pos : this.perChunkTotalCount.keySet())
        {
            counts.add(new TileEntityCountsPerChunkHolder(pos, this.perChunkTotalCount.get(pos), this.perChunkTickingCount.get(pos)));
        }

        Collections.sort(counts);

        EntityCountDump dump = new EntityCountDump(4);
        dump.addTitle("Chunk", "Total Count", "Ticking", "Region");
        dump.addHeader("Loaded TileEntities by chunk:");

        for (TileEntityCountsPerChunkHolder holder : counts)
        {
            dump.addData(
                    String.format("[%5d, %5d]", holder.pos.x, holder.pos.z),
                    String.valueOf(holder.count),
                    String.valueOf(holder.tickingCount),
                    String.format("r.%d.%d", holder.pos.x >> 5, holder.pos.z >> 5));
        }

        dump.addFooter(String.format("In total there were %d loaded TileEntities", this.totalCount));
        dump.addFooter(String.format("in %d chunks, of which %d are ticking.",
                this.getLoadedChunkCount() - this.chunksWithZeroCount, this.tickingCount));

        return dump;
    }
}
