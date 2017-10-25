package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.datadump.EntityCountDump;
import fi.dy.masa.tellme.util.EntityInfo;

public class EntitiesPerTypeCounter extends ChunkProcessor
{
    private Map<Class <? extends Entity>, Integer> perTypeCount = new HashMap<Class <? extends Entity>, Integer>();
    private int totalCount;

    @Override
    public void processChunk(Chunk chunk)
    {
        ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
        int total = 0;

        for (int i = 0; i < entityLists.length; i++)
        {
            Iterator<Entity> iter = entityLists[i].iterator();
            total += entityLists[i].size();

            while (iter.hasNext())
            {
                Entity entity = iter.next();
                Integer countInt = this.perTypeCount.get(entity.getClass());
                int count = countInt != null ? countInt + 1 : 1;
                this.perTypeCount.put(entity.getClass(), count);
            }
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
        List<EntitiesPerTypeHolder> counts = new ArrayList<EntitiesPerTypeHolder>();

        for (Class <? extends Entity> clazz : this.perTypeCount.keySet())
        {
            counts.add(new EntitiesPerTypeHolder(clazz, this.perTypeCount.get(clazz)));
        }

        Collections.sort(counts);

        EntityCountDump dump = new EntityCountDump(2);
        dump.addTitle("Entity type", "Count");
        dump.addHeader("Loaded entities by entity type:");

        for (EntitiesPerTypeHolder holder : counts)
        {
            dump.addData(EntityInfo.getEntityNameFromClass(holder.clazz), String.valueOf(holder.count));
        }

        dump.addFooter(String.format("In total there were %d loaded entities in %d chunks.",
                this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        return dump;
    }
}
