package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Map;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class BlockEntitiesLister extends ChunkProcessorBase
{
    private final DataDump dump;
    private int totalCount;

    public BlockEntitiesLister(DataDump.Format format)
    {
        super(format);

        DataDump dump = new DataDump(6, format);

        dump.setSort(true);
        dump.addHeader("Loaded BlockEntities by chunk:");
        dump.addTitle("Region", "Chunk", "Position", "Tile", "Class");

        this.dump = dump;
    }

    @Override
    public void processChunk(WorldChunk chunk)
    {
        Map<BlockPos, BlockEntity> map = chunk.getBlockEntities();
        int count = map.size();

        if (count == 0)
        {
            ++this.chunksWithZeroCount;
        }
        else
        {
            for (Map.Entry<BlockPos, BlockEntity> entry : map.entrySet())
            {
                BlockEntity te = entry.getValue();
                BlockPos pos = te.getPos();
                int x = pos.getX();
                int z = pos.getZ();

                this.dump.addData(
                        String.format("r.%d.%d", x >> 9, z >> 9),
                        String.format("[%5d, %5d]", x >> 4, z >> 4),
                        String.format("%6d, %3d, %6d", x, pos.getY(), z),
                        BlockInfo.getBlockEntityNameFor(te.getType()),
                        te.getClass().getName());
            }

            this.totalCount += count;
        }
    }

    @Override
    public DataDump getDump()
    {
        DataDump dump = this.dump;

        dump.clearFooter();
        dump.addFooter(String.format("In total there were %d loaded BlockEntities in %d chunks",
                                     this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        return dump;
    }
}
