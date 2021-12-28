package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ChunkProcessorTileEntityCounterPerType extends ChunkProcessorBase
{
    private Object2IntOpenHashMap<TileEntityType<?>> perTypeCount = new Object2IntOpenHashMap<>();
    private int totalCount;
    private int tickingCount;

    public ChunkProcessorTileEntityCounterPerType(DataDump.Format format)
    {
        super(format);
    }

    @Override
    public void processChunk(Chunk chunk)
    {
        Map<BlockPos, TileEntity> map = chunk.getBlockEntities();
        final int total = map.size();

        if (total > 0)
        {
            int tickingCount = 0;

            for (TileEntity te : map.values())
            {
                this.perTypeCount.addTo(te.getType(), 1);

                if (te instanceof ITickableTileEntity)
                {
                    ++tickingCount;
                }
            }

            this.totalCount += total;
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
        List<TileEntitiesPerTypeHolder> counts = new ArrayList<>();

        for (Map.Entry<TileEntityType<?>, Integer> entry : this.perTypeCount.object2IntEntrySet())
        {
            TileEntityType<?> type = entry.getKey();
            TileEntity te = type.create();
            counts.add(new TileEntitiesPerTypeHolder(type, te != null ? te.getClass() : null, entry.getValue()));
        }

        Collections.sort(counts);

        DataDump dump = new DataDump(4, this.format);

        dump.setSort(true).setSortColumn(2).setSortReverse(true);
        dump.addHeader("Loaded TileEntities by type:");
        dump.addTitle("TileEntity type", "Class", "Count", "Is ticking?");

        for (TileEntitiesPerTypeHolder holder : counts)
        {
            Class<? extends TileEntity> clazz = holder.clazz;
            String ticking = clazz != null && ITickableTileEntity.class.isAssignableFrom(clazz) ? "yes" : "no";

            dump.addData(
                    BlockInfo.getBlockEntityNameFor(holder.type),
                    clazz != null ? clazz.getName() : "<null>",
                    String.valueOf(holder.count),
                    ticking);
        }

        dump.addFooter(String.format("In total there were %d loaded TileEntities", this.totalCount));
        dump.addFooter(String.format("in %d chunks, of which %d are ticking.",
                this.getLoadedChunkCount() - this.chunksWithZeroCount, this.tickingCount));

        return dump;
    }

    public static class TileEntitiesPerTypeHolder implements Comparable<TileEntitiesPerTypeHolder>
    {
        public final TileEntityType<?> type;
        @Nullable public final Class<? extends TileEntity> clazz;
        public final int count;

        public TileEntitiesPerTypeHolder(TileEntityType<?> type, @Nullable Class<? extends TileEntity> clazz, int count)
        {
            this.type = type;
            this.clazz = clazz;
            this.count = count;
        }

        @Override
        public int compareTo(TileEntitiesPerTypeHolder other)
        {
            if (this.count == other.count)
            {
                String nameThis = BlockInfo.getBlockEntityNameFor(this.type);
                String nameOther = BlockInfo.getBlockEntityNameFor(other.type);
                return nameThis.compareTo(nameOther);
            }

            return this.count > other.count ? -1 : 1;
        }
    }
}
