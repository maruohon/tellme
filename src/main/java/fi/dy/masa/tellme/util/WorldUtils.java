package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

public class WorldUtils
{
    public static Collection<Chunk> getLoadedChunks(World world)
    {
        IChunkProvider provider = world.getChunkProvider();

        if (provider instanceof ChunkProviderServer)
        {
            return ((ChunkProviderServer) provider).getLoadedChunks();
        }

        EntityPlayer player = Minecraft.getMinecraft().player;

        if (player != null)
        {
            BlockPos pos = player.getPosition();
            int cX = pos.getX() >> 4;
            int cZ = pos.getZ() >> 4;
            int radius = Minecraft.getMinecraft().gameSettings.renderDistanceChunks + 1;
            List<Chunk> chunks = new ArrayList<Chunk>();

            for (int z = cZ - radius; z <= (cZ + radius); z++)
            {
                for (int x = cX - radius; x <= (cX + radius); x++)
                {
                    Chunk chunk = provider.getLoadedChunk(x, z);

                    if (chunk != null)
                    {
                        chunks.add(chunk);
                    }
                }
            }

            return chunks;
        }

        return Collections.emptyList();

    }

    public static int getLoadedChunkCount(World world)
    {
        return world != null && world.getChunkProvider() instanceof ChunkProviderServer ?
                ((ChunkProviderServer) world.getChunkProvider()).getLoadedChunkCount() : 0;
    }

    public static List<Chunk> loadAndGetChunks(World world, BlockPos centerPos, int radius)
    {
        ChunkPos center = new ChunkPos(centerPos.getX() >> 4, centerPos.getZ() >> 4);
        List<Chunk> chunks = new ArrayList<>();

        for (int cZ = center.z - radius; cZ <= center.z + radius; cZ++)
        {
            for (int cX = center.x - radius; cX <= center.x + radius; cX++)
            {
                chunks.add(world.getChunkFromChunkCoords(cX, cZ));
            }
        }

        return chunks;
    }

    public static List<Chunk> loadAndGetChunks(World world, ChunkPos posMin, ChunkPos posMax)
    {
        List<Chunk> chunks = new ArrayList<>();

        for (int cZ = posMin.z; cZ <= posMax.z; cZ++)
        {
            for (int cX = posMin.x; cX <= posMax.x; cX++)
            {
                chunks.add(world.getChunkFromChunkCoords(cX, cZ));
            }
        }

        return chunks;
    }

    public static BlockPos getSpawnPoint(@Nonnull World world)
    {
        BlockPos pos = null;

        if (world instanceof WorldServer)
        {
            // This is mostly for The End dimension, others return null here
            pos = ((WorldServer) world).getSpawnCoordinate();
        }

        if (pos == null)
        {
            pos = world.getSpawnPoint();
        }

        return pos;
    }
}
