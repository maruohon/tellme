package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.mixin.IMixinThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

public class WorldUtils
{
    public static int getLoadedChunkCount(ServerWorld world)
    {
        //return ((ServerChunkManager) world.getChunkManager()).getTotalChunksLoadedCount();
        Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = ((IMixinThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage).tellmeGetChunkHolders();
        return chunkHolders.size();
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
        Identifier id = world.getRegistryManager().getDimensionTypes().getId(world.getDimension());
        return id != null ? id.toString() : "?";
    }
}
