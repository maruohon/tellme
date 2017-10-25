package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.datadump.EntityCountDump;

public class TileEntitiesPerTypeCounter extends ChunkProcessor
{
    private Map<Class <? extends TileEntity>, Integer> perTypeCount = new HashMap<Class <? extends TileEntity>, Integer>();
    private int totalCount;

    @Override
    public void processChunk(Chunk chunk)
    {
        Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
        int total = map.size();

        for (TileEntity te : map.values())
        {
            Integer countInt = this.perTypeCount.get(te.getClass());
            int count = countInt != null ? countInt + 1 : 1;
            this.perTypeCount.put(te.getClass(), count);
        }

        if (total == 0)
        {
            this.chunksWithZeroCount++;
        }
        else
        {
            this.totalCount += total;
        }
    }

    @Override
    public EntityCountDump createDump(World world)
    {
        List<TileEntitiesPerTypeHolder> counts = new ArrayList<TileEntitiesPerTypeHolder>();

        for (Class <? extends TileEntity> clazz : this.perTypeCount.keySet())
        {
            counts.add(new TileEntitiesPerTypeHolder(clazz, this.perTypeCount.get(clazz)));
        }

        Collections.sort(counts);

        EntityCountDump dump = new EntityCountDump(3);
        dump.addTitle("TileEntity type", "Count", "Is ticking?");
        dump.addHeader("Loaded TileEntities by type:");

        for (TileEntitiesPerTypeHolder holder : counts)
        {
            String ticking = ITickable.class.isAssignableFrom(holder.clazz) ? "yes" : "no";
            dump.addData(holder.clazz.getName(), String.valueOf(holder.count), ticking);
        }

        dump.addFooter(String.format("In total there were %d loaded TileEntities in %d chunks.",
                this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        return dump;
    }
}
