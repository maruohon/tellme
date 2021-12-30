package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ChunkProcessorTileEntityCounterPerType extends ChunkProcessorBase
{
    private Object2IntOpenHashMap<BlockEntityType<?>> perTypeCount = new Object2IntOpenHashMap<>();
    private int totalCount;
    private int tickingCount;

    public ChunkProcessorTileEntityCounterPerType(DataDump.Format format)
    {
        super(format);
    }

    @Override
    public void processChunk(LevelChunk chunk)
    {
        Map<BlockPos, BlockEntity> map = chunk.getBlockEntities();
        final int total = map.size();

        if (total > 0)
        {
            int tickingCount = 0;

            for (BlockEntity te : map.values())
            {
                this.perTypeCount.addTo(te.getType(), 1);

                if (te instanceof TickableBlockEntity)
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

        for (Map.Entry<BlockEntityType<?>, Integer> entry : this.perTypeCount.object2IntEntrySet())
        {
            BlockEntityType<?> type = entry.getKey();
            BlockEntity te = type.create();
            counts.add(new TileEntitiesPerTypeHolder(type, te != null ? te.getClass() : null, entry.getValue()));
        }

        Collections.sort(counts);

        DataDump dump = new DataDump(4, this.format);

        dump.setSort(true).setSortColumn(2).setSortReverse(true);
        dump.addHeader("Loaded TileEntities by type:");
        dump.addTitle("TileEntity type", "Class", "Count", "Is ticking?");

        for (TileEntitiesPerTypeHolder holder : counts)
        {
            Class<? extends BlockEntity> clazz = holder.clazz;
            String ticking = clazz != null && TickableBlockEntity.class.isAssignableFrom(clazz) ? "yes" : "no";

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
        public final BlockEntityType<?> type;
        @Nullable public final Class<? extends BlockEntity> clazz;
        public final int count;

        public TileEntitiesPerTypeHolder(BlockEntityType<?> type, @Nullable Class<? extends BlockEntity> clazz, int count)
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
