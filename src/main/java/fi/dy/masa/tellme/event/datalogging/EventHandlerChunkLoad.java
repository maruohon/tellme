package fi.dy.masa.tellme.event.datalogging;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventHandlerChunkLoad
{
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event)
    {
        DataLogger.instance().onChunkEvent(DataType.CHUNK_LOAD, event.getChunk());
    }
}
