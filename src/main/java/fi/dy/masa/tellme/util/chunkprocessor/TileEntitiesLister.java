package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.datadump.EntityCountDump;

public class TileEntitiesLister extends ChunkProcessor
{
    private Multimap<ChunkPos, TileHolder> perChunkTiles = MultimapBuilder.hashKeys().arrayListValues().build();
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

            for (Map.Entry<BlockPos, TileEntity> entry : map.entrySet())
            {
                TileEntity te = entry.getValue();
                this.perChunkTiles.put(pos, new TileHolder(entry.getKey(), te.getClass()));

                if (te instanceof ITickable)
                {
                    tickingCount++;
                }
            }

            this.totalCount += count;
            this.tickingCount += tickingCount;
        }
    }

    @Override
    public EntityCountDump createDump(World world)
    {
        EntityCountDump dump = new EntityCountDump(4);
        dump.addTitle("Chunk", "Tile", "Ticking", "Region");
        dump.addHeader("Loaded TileEntities by chunk:");

        for (ChunkPos chunkPos : this.perChunkTiles.keySet())
        {
            List<TileHolder> tiles = new ArrayList<>(this.perChunkTiles.get(chunkPos));

            Collections.sort(tiles);

            for (TileHolder holder : tiles)
            {
                dump.addData(
                        String.format("[%5d, %5d]", chunkPos.x, chunkPos.z),
                        String.valueOf(holder.clazz.getName()),
                        ITickable.class.isAssignableFrom(holder.clazz) ? "yes" : "no",
                        String.format("r.%d.%d", chunkPos.x >> 5, chunkPos.z >> 5));
            }
        }

        dump.addFooter(String.format("In total there were %d loaded TileEntities", this.totalCount));
        dump.addFooter(String.format("in %d chunks, of which %d are ticking.",
                this.getLoadedChunkCount() - this.chunksWithZeroCount, this.tickingCount));

        return dump;
    }

    private static class TileHolder implements Comparable<TileHolder>
    {
        public final Class <? extends TileEntity> clazz;
        public final BlockPos pos;

        public TileHolder(BlockPos pos, Class <? extends TileEntity> clazz)
        {
            this.pos = pos;
            this.clazz = clazz;
        }

        @Override
        public int compareTo(TileHolder other)
        {
            String nameThis = this.clazz.getName();
            String nameOther = other.clazz.getName();
            int result = nameThis.compareTo(nameOther);

            if (result != 0)
            {
                return result;
            }

            return this.pos.compareTo(other.pos);
        }
    }
}
