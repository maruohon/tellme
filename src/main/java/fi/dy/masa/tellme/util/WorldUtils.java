package fi.dy.masa.tellme.util;

import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;

public class WorldUtils
{
    public static int getLoadedChunkCount(World world)
    {
        return world != null && world.getChunkProvider() instanceof ChunkProviderServer ?
                ((ChunkProviderServer) world.getChunkProvider()).getLoadedChunkCount() : 0;
    }
}
