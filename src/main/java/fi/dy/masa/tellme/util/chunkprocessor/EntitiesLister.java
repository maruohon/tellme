package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.malilib.util.EntityUtils;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.EntityCountDump;

public class EntitiesLister extends ChunkProcessorLoadedChunks
{
    private List<Entity> entities = new ArrayList<Entity>();
    private int totalCount;

    @Override
    public void processChunk(Chunk chunk)
    {
        ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
        int total = 0;

        for (int i = 0; i < entityLists.length; i++)
        {
            Iterator<Entity> iter = entityLists[i].iterator();

            while (iter.hasNext())
            {
                this.entities.add(iter.next());
            }

            total += entityLists[i].size();
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
        EntityCountDump dump = new EntityCountDump(5);
        dump.addTitle("Name", "Health", "Location", "Chunk", "Region");
        dump.addHeader("All currently loaded entities:");
        dump.setColumnAlignment(1, Alignment.RIGHT); // health
        dump.setSort(true);

        for (Entity entity : this.entities)
        {
            BlockPos pos = entity.getPosition();

            dump.addData(
                    entity.getName(),
                    entity instanceof EntityLivingBase ? String.format("%.2f", ((EntityLivingBase) entity).getHealth()) : "-",
                    String.format("x = %8.2f, y = %8.2f, z = %8.2f", EntityUtils.getX(entity), EntityUtils.getY(entity), EntityUtils.getZ(entity)),
                                  String.format("[%5d, %5d]", pos.getX() >> 4, pos.getZ() >> 4),
                                  String.format("r.%d.%d", pos.getX() >> 9, pos.getZ() >> 9));
        }

        dump.addFooter(String.format("In total there were %d loaded entities in %d chunks.",
                this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        return dump;
    }
}
