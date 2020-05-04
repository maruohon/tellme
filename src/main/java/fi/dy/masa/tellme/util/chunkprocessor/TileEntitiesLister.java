package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Map;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.util.BlockInfo;

public class TileEntitiesLister extends ChunkProcessorBase
{
    private final DataDump dump;
    private int totalCount;
    private int tickingCount;

    public TileEntitiesLister(DataDump.Format format)
    {
        super(format);

        DataDump dump = new DataDump(6, format);

        dump.setSort(true);
        dump.setRepeatTitleAtBottom(false);

        dump.addTitle("Region", "Chunk", "Position", "Tile", "Class", "Ticking");
        dump.addHeader("Loaded TileEntities by chunk:");

        this.dump = dump;
    }

    @Override
    public void processChunk(Chunk chunk)
    {
        Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
        int count = chunk.getTileEntityMap().size();

        if (count == 0)
        {
            ++this.chunksWithZeroCount;
        }
        else
        {
            int tickingCount = 0;

            for (Map.Entry<BlockPos, TileEntity> entry : map.entrySet())
            {
                TileEntity te = entry.getValue();
                boolean ticking = false;

                if (te instanceof ITickableTileEntity)
                {
                    tickingCount++;
                    ticking = true;
                }

                BlockPos pos = te.getPos();
                int x = pos.getX();
                int z = pos.getZ();

                this.dump.addData(
                        String.format("r.%d.%d", x >> 9, z >> 9),
                        String.format("[%5d, %5d]", x >> 4, z >> 4),
                        String.format("%6d, %3d, %6d", x, pos.getY(), z),
                        BlockInfo.getBlockEntityNameFor(te.getType()),
                        te.getClass().getName(),
                        ticking ? "yes" : "");
            }

            this.totalCount += count;
            this.tickingCount += tickingCount;
        }
    }

    @Override
    public DataDump getDump()
    {
        DataDump dump = this.dump;

        dump.clearFooter();
        dump.addFooter(String.format("In total there were %d loaded TileEntities", this.totalCount));
        dump.addFooter(String.format("in %d chunks, of which %d are ticking.",
                this.getLoadedChunkCount() - this.chunksWithZeroCount, this.tickingCount));

        return dump;
    }
}
