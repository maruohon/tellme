package fi.dy.masa.tellme.event.datalogging;

import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventHandlerChunkUnload
{
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event)
    {
        DataLogger.instance().onChunkEvent(DataType.CHUNK_UNLOAD, event.getChunk());
    }
}
