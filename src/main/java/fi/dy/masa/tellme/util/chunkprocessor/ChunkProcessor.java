package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Collection;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.EntityCountDump;
import fi.dy.masa.tellme.util.EntityInfo;

public abstract class ChunkProcessor
{
    protected int chunksWithZeroCount;
    private int loadedChunks;
    private int unloadedChunks;

    public int getLoadedChunkCount()
    {
        return this.loadedChunks;
    }

    public int getUnloadedChunkCount()
    {
        return this.unloadedChunks;
    }

    public int getChunksWithZeroCount()
    {
        return this.chunksWithZeroCount;
    }

    protected abstract void processChunk(Chunk chunk);

    public abstract EntityCountDump createDump(World world);

    public void processAllLoadedChunks(World world)
    {
        Collection<Chunk> loadedChunks = TellMe.proxy.getLoadedChunks(world);

        for (Chunk chunk : loadedChunks)
        {
            this.processChunk(chunk);
            this.loadedChunks++;
        }
    }

    public void processChunksInArea(World world, ChunkPos pos1, ChunkPos pos2)
    {
        IChunkProvider provider = world.getChunkProvider();

        for (int chunkZ = pos1.z; chunkZ <= pos2.z; chunkZ++)
        {
            for (int chunkX = pos1.x; chunkX <= pos2.x; chunkX++)
            {
                Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);

                if (chunk != null)
                {
                    this.processChunk(chunk);
                    this.loadedChunks++;
                }
                else
                {
                    this.unloadedChunks++;
                }
            }
        }
    }

    public static class EntitiesPerTypeHolder implements Comparable<EntitiesPerTypeHolder>
    {
        public final Class <? extends Entity> clazz;
        public final int count;

        public EntitiesPerTypeHolder(Class <? extends Entity> clazz, int count)
        {
            this.clazz = clazz;
            this.count = count;
        }

        @Override
        public int compareTo(EntitiesPerTypeHolder other)
        {
            if (this.count == other.count)
            {
                String nameThis = EntityInfo.getEntityNameFromClass(this.clazz);
                String nameOther = EntityInfo.getEntityNameFromClass(other.clazz);

                if (nameThis == null)
                {
                    nameThis = this.clazz.getSimpleName();
                }

                if (nameOther == null)
                {
                    nameOther = other.clazz.getSimpleName();
                }

                return nameThis.compareTo(nameOther);
            }

            return this.count > other.count ? -1 : 1;
        }
    }

    public static class CountsPerChunkHolder implements Comparable<CountsPerChunkHolder>
    {
        public final ChunkPos pos;
        public final int count;

        public CountsPerChunkHolder(ChunkPos pos, int count)
        {
            this.pos = pos;
            this.count = count;
        }

        @Override
        public int compareTo(CountsPerChunkHolder other)
        {
            if (this.count == other.count)
            {
                return 0;
            }

            return this.count > other.count ? -1 : 1;
        }
    }

    public static class TileEntityCountsPerChunkHolder extends CountsPerChunkHolder
    {
        public final int tickingCount;

        public TileEntityCountsPerChunkHolder(ChunkPos pos, int totalCount, int tickingCount)
        {
            super(pos, totalCount);
            this.tickingCount = tickingCount;
        }
    }

    public static class TileEntitiesPerTypeHolder implements Comparable<TileEntitiesPerTypeHolder>
    {
        public final Class <? extends TileEntity> clazz;
        public final int count;

        public TileEntitiesPerTypeHolder(Class <? extends TileEntity> clazz, int count)
        {
            this.clazz = clazz;
            this.count = count;
        }

        @Override
        public int compareTo(TileEntitiesPerTypeHolder other)
        {
            if (this.count == other.count)
            {
                String nameThis = this.clazz.getName();
                String nameOther = other.clazz.getName();
                return nameThis.compareTo(nameOther);
            }

            return this.count > other.count ? -1 : 1;
        }
    }
}
