package fi.dy.masa.tellme.util.chunkprocessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.TypeFilterableList;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class EntitiesLister extends ChunkProcessorBase
{
    private final DataDump dump;
    private int totalCount;

    public EntitiesLister(DataDump.Format format)
    {
        super(format);

        DataDump dump = new DataDump(5, format);
        dump.setColumnAlignment(1, Alignment.RIGHT); // health
        dump.setSort(true);

        dump.addTitle("Name", "Health", "Location", "Chunk", "Region");
        dump.addHeader("All currently loaded entities:");

        this.dump = dump;
    }

    @Override
    public void processChunk(WorldChunk chunk)
    {
        TypeFilterableList<Entity>[] entityLists = chunk.getEntitySectionArray();
        int total = 0;

        for (int i = 0; i < entityLists.length; i++)
        {
            TypeFilterableList<Entity> map = entityLists[i];

            for (Entity entity : map)
            {
                double x = entity.x;
                double y = entity.y;
                double z = entity.z;
                int ix = (int) Math.floor(x);
                int iz = (int) Math.floor(z);

                this.dump.addData(
                        entity.getName().getString(),
                        entity instanceof LivingEntity ? String.format("%.2f", ((LivingEntity) entity).getHealth()) : "-",
                        String.format("x = %8.2f, y = %8.2f, z = %8.2f", x, y, z),
                        String.format("[%5d, %5d]", ix >> 4, iz >> 4),
                        String.format("r.%d.%d", ix >> 9, iz >> 9));
            }

            total += entityLists[i].size();
        }

        if (total > 0)
        {
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
        this.dump.clearFooter();
        this.dump.addFooter(String.format("In total there were %d loaded entities in %d chunks.",
                this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));

        return this.dump;
    }
}
