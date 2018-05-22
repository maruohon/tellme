package fi.dy.masa.tellme.event.datalogging;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventHandlerChunkUnload
{
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event)
    {
        Chunk chunk = event.getChunk();

        if (chunk.getWorld().isRemote == false)
        {
            DataLogger.instance(chunk.getWorld().provider.getDimension()).onChunkEvent(DataType.CHUNK_UNLOAD, chunk);
        }
    }
}
