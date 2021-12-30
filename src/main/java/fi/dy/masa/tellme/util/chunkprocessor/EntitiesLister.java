package fi.dy.masa.tellme.util.chunkprocessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import fi.dy.masa.tellme.util.WorldUtils;
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
    public void processChunk(LevelChunk chunk)
    {
        int totalBefore = this.totalCount;
        ChunkPos pos = chunk.getPos();
        WorldUtils.processEntitiesInChunk(chunk.getLevel(), pos.x, pos.z, this::entityConsumer);

        if (totalBefore == this.totalCount)
        {
            ++this.chunksWithZeroCount;
        }
    }

    private void entityConsumer(Entity entity)
    {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        int ix = (int) Math.floor(x);
        int iz = (int) Math.floor(z);

        ++this.totalCount;
        this.dump.addData(
                entity.getName().getString(),
                entity instanceof LivingEntity ? String.format("%.2f", ((LivingEntity) entity).getHealth()) : "-",
                String.format("x = %8.2f, y = %8.2f, z = %8.2f", x, y, z),
                String.format("[%5d, %5d]", ix >> 4, iz >> 4),
                String.format("r.%d.%d", ix >> 9, iz >> 9));
    }

    @Override
    public DataDump getDump()
    {
        this.dump.clearFooter();
        this.dump.addFooter(String.format("In total there were %d loaded entities in %d chunks.",
                this.totalCount, this.getLoadedChunkCount() - this.chunksWithZeroCount));
        this.dump.addFooter(String.format("And %d chunks without entities", this.chunksWithZeroCount));

        return this.dump;
    }
}
