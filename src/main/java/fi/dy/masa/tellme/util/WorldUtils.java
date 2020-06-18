package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;

public class WorldUtils
{
    public static int getLoadedChunkCount(World world)
    {
        return world != null && world.getChunkManager() instanceof ServerChunkManager ?
                ((ServerChunkManager) world.getChunkManager()).getLoadedChunkCount() : 0;
    }

    public static List<WorldChunk> loadAndGetChunks(World world, BlockPos centerPos, int radius)
    {
        ChunkPos center = new ChunkPos(centerPos.getX() >> 4, centerPos.getZ() >> 4);
        List<WorldChunk> chunks = new ArrayList<>();

        for (int cZ = center.z - radius; cZ <= center.z + radius; cZ++)
        {
            for (int cX = center.x - radius; cX <= center.x + radius; cX++)
            {
                chunks.add(world.method_8497(cX, cZ));
            }
        }

        return chunks;
    }

    public static List<WorldChunk> loadAndGetChunks(World world, ChunkPos posMin, ChunkPos posMax)
    {
        List<WorldChunk> chunks = new ArrayList<>();

        for (int cZ = posMin.z; cZ <= posMax.z; cZ++)
        {
            for (int cX = posMin.x; cX <= posMax.x; cX++)
            {
                chunks.add(world.method_8497(cX, cZ));
            }
        }

        return chunks;
    }

    public static BlockPos getSpawnPoint(@Nonnull World world)
    {
        BlockPos pos = null;

        if (world instanceof ServerWorld)
        {
            // This is mostly for The End dimension, others return null here
            pos = ((ServerWorld) world).getForcedSpawnPoint();
        }

        if (pos == null)
        {
            pos = world.getSpawnPos();
        }

        return pos;
    }

    public static String getDimensionId(World world)
    {
        Identifier id = DimensionType.getId(world.getDimension().getType());
        return id != null ? id.toString() : "<null>";
    }
}
