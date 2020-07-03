package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;

public class WorldUtils
{
    public static int getLoadedChunkCount(World world)
    {
        return world != null && world.getChunkManager() instanceof ServerChunkManager ?
                ((ServerChunkManager) world.getChunkManager()).getTotalChunksLoadedCount() : 0;
    }

    public static List<WorldChunk> loadAndGetChunks(World world, ChunkPos posMin, ChunkPos posMax)
    {
        List<WorldChunk> chunks = new ArrayList<>();

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
        Identifier id = DimensionType.getId(world.getDimension().getType());
        return id != null ? id.toString() : "<null>";
    }
}
