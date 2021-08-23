package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ChunkProcessorTileEntityCounterPerType extends ChunkProcessorBase
{
    private final Object2IntOpenHashMap<BlockEntityType<?>> perTypeCount = new Object2IntOpenHashMap<>();
    private int totalCount;

    public ChunkProcessorTileEntityCounterPerType(DataDump.Format format)
    {
        super(format);
    }

    @Override
    public void processChunk(WorldChunk chunk)
    {
        Map<BlockPos, BlockEntity> map = chunk.getBlockEntities();
        final int total = map.size();

        if (total > 0)
        {
            for (BlockEntity te : map.values())
            {
                this.perTypeCount.addTo(te.getType(), 1);
            }

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
        List<TileEntitiesPerTypeHolder> counts = new ArrayList<>();

        for (Map.Entry<BlockEntityType<?>, Integer> entry : this.perTypeCount.object2IntEntrySet())
        {
            BlockEntityType<?> type = entry.getKey();
            counts.add(new TileEntitiesPerTypeHolder(type, entry.getValue()));
        }

        Collections.sort(counts);

        DataDump dump = new DataDump(2, this.format);

        dump.setSort(true).setSortColumn(2).setSortReverse(true);
        dump.addHeader("Loaded TileEntities by type:");
        dump.addTitle("TileEntity type", "Count");

        for (TileEntitiesPerTypeHolder holder : counts)
        {
            dump.addData(BlockInfo.getBlockEntityNameFor(holder.type), String.valueOf(holder.count));
        }

        dump.addFooter(String.format("In total there were %d loaded TileEntities in %d chunks",
                                     this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        return dump;
    }

    public static class TileEntitiesPerTypeHolder implements Comparable<TileEntitiesPerTypeHolder>
    {
        public final BlockEntityType<?> type;
        public final int count;

        public TileEntitiesPerTypeHolder(BlockEntityType<?> type, int count)
        {
            this.type = type;
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
