package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import fi.dy.masa.tellme.command.CommandUtils;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.datadump.DataDump;

public abstract class ChunkProcessorBase
{
    protected final DataDump.Format format;
    protected int chunksWithZeroCount;
    @Nullable protected Vec3 minPos;
    @Nullable protected Vec3 maxPos;
    private int loadedChunks;
    private int unloadedChunks;

    protected ChunkProcessorBase(DataDump.Format format)
    {
        this.format = format;
    }

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

    public void setBoxCorners(Vec3 pos1, Vec3 pos2)
    {
        this.minPos = CommandUtils.getMinCornerVec3d(pos1, pos2);
        this.maxPos = CommandUtils.getMaxCornerVec3d(pos1, pos2);
    }

    public void processChunks(Collection<LevelChunk> chunks)
    {
        for (LevelChunk chunk : chunks)
        {
            this.processChunk(chunk);
            ++this.loadedChunks;
        }
    }

    public void processChunksInArea(Level world, ChunkPos pos1, ChunkPos pos2)
    {
        ChunkSource provider = world.getChunkSource();
        final int minCX = Math.min(pos1.x, pos2.x);
        final int minCZ = Math.min(pos1.z, pos2.z);
        final int maxCX = Math.max(pos1.x, pos2.x);
        final int maxCZ = Math.max(pos1.z, pos2.z);

        for (int cz = minCZ; cz <= maxCZ; ++cz)
        {
            for (int cx = minCX; cx <= maxCX; ++cx)
            {
                LevelChunk chunk = provider.getChunk(cx, cz, false);

                if (chunk != null)
                {
                    this.processChunk(chunk);
                    ++this.loadedChunks;
                }
                else
                {
                    ++this.unloadedChunks;
                }
            }
        }
    }

    protected abstract void processChunk(LevelChunk chunk);

    public abstract DataDump getDump();

    public static class EntitiesPerTypeHolder implements Comparable<EntitiesPerTypeHolder>
    {
        public final EntityType<?> type;
        public final int count;

        public EntitiesPerTypeHolder(EntityType<?> type, int count)
        {
            this.type = type;
            this.count = count;
        }

        @Override
        public int compareTo(EntitiesPerTypeHolder other)
        {
            if (this.count == other.count)
            {
                String nameThis = EntityInfo.getEntityNameFor(this.type);
                String nameOther = EntityInfo.getEntityNameFor(other.type);
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
}
