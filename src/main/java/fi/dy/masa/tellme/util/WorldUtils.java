package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;

public class WorldUtils
{
    public static int getLoadedChunkCount(World world)
    {
        if (world != null && world.getChunkSource() instanceof ServerChunkProvider)
        {
            return ((ServerChunkProvider) world.getChunkSource()).chunkMap.getTickingGenerated();
        }

        return 0;
    }

    public static List<Chunk> loadAndGetChunks(World world, ChunkPos posMin, ChunkPos posMax)
    {
        List<Chunk> chunks = new ArrayList<>();

        for (int cZ = posMin.z; cZ <= posMax.z; cZ++)
        {
            for (int cX = posMin.x; cX <= posMax.x; cX++)
            {
                chunks.add(world.getChunk(cX, cZ));
            }
        }

        return chunks;
    }

    public static String getDimensionId(World world)
    {
        ResourceLocation id = world.registryAccess().dimensionTypes().getKey(world.dimensionType());
        return id != null ? id.toString() : "?";
    }
}
