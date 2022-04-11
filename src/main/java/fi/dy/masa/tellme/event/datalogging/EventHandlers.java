package fi.dy.masa.tellme.event.datalogging;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventHandlers
{
    public static void onChunkLoad(Chunk chunk)
    {
        World world = chunk.getWorld();

        if (EventManager.isLoggingEnabled(DataType.CHUNK_LOAD) && world.isRemote == false)
        {
            DataLogger.instance(WorldUtils.getDimensionId(world)).onChunkEvent(DataType.CHUNK_LOAD, chunk);
        }
    }

    public static void onChunkUnload(Chunk chunk)
    {
        World world = chunk.getWorld();

        if (EventManager.isLoggingEnabled(DataType.CHUNK_UNLOAD) && world.isRemote == false)
        {
            DataLogger.instance(WorldUtils.getDimensionId(world)).onChunkEvent(DataType.CHUNK_UNLOAD, chunk);
        }
    }

    public static void onEntityJoinWorld(Entity entity)
    {
        World world = entity.getEntityWorld();

        if (EventManager.isLoggingEnabled(DataType.ENTITY_JOIN_WORLD) && world.isRemote == false)
        {
            DataLogger.instance(WorldUtils.getDimensionId(world)).onEntityEvent(DataType.ENTITY_JOIN_WORLD, entity);
        }
    }
}
