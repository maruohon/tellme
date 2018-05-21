package fi.dy.masa.tellme.event.datalogging;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventHandlerChunkLoad
{
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event)
    {
        Chunk chunk = event.getChunk();
        DataLogger.instance(chunk.getWorld().provider.getDimension()).onChunkEvent(DataType.CHUNK_LOAD, chunk);
    }
}
