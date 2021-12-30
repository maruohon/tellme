package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class WorldUtils
{
    public static int getLoadedChunkCount(Level world)
    {
        if (world != null && world.getChunkSource() instanceof ServerChunkCache)
        {
            return ((ServerChunkCache) world.getChunkSource()).chunkMap.getTickingGenerated();
        }

        return 0;
    }

    public static List<LevelChunk> loadAndGetChunks(Level world, ChunkPos posMin, ChunkPos posMax)
    {
        List<LevelChunk> chunks = new ArrayList<>();

        for (int cZ = posMin.z; cZ <= posMax.z; cZ++)
        {
            for (int cX = posMin.x; cX <= posMax.x; cX++)
            {
                chunks.add(world.getChunk(cX, cZ));
            }
        }

        return chunks;
    }

    public static String getDimensionId(Level world)
    {
        ResourceLocation id = world.registryAccess().dimensionTypes().getKey(world.dimensionType());
        return id != null ? id.toString() : "?";
    }
}
