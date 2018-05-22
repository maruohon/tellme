package fi.dy.masa.tellme.event.datalogging;

import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EventHandlers
{
    public static void onChunkLoad(Chunk chunk)
    {
        World world = chunk.getWorld();

        if (EventManager.isLoggingEnabled(DataType.CHUNK_LOAD) && world.isRemote == false)
        {
            int dimension = world.provider.getDimensionType().getId();
            DataLogger.instance(dimension).onChunkEvent(DataType.CHUNK_LOAD, chunk);
        }
    }

    public static void onChunkUnload(Chunk chunk)
    {
        World world = chunk.getWorld();

        if (EventManager.isLoggingEnabled(DataType.CHUNK_UNLOAD) && world.isRemote == false)
        {
            int dimension = world.provider.getDimensionType().getId();
            DataLogger.instance(dimension).onChunkEvent(DataType.CHUNK_UNLOAD, chunk);
        }
    }

    public static void onEntityJoinWorld(Entity entity)
    {
        World world = entity.getEntityWorld();

        if (EventManager.isLoggingEnabled(DataType.ENTITY_JOIN_WORLD) && world.isRemote == false)
        {
            int dimension = world.provider.getDimensionType().getId();
            DataLogger.instance(dimension).onEntityEvent(DataType.ENTITY_JOIN_WORLD, entity);
        }
    }
}
