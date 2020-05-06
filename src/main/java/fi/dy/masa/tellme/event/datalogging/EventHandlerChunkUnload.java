package fi.dy.masa.tellme.event.datalogging;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandlerChunkUnload
{
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event)
    {
        IChunk chunk = event.getChunk();
        World world = chunk.getWorldForge().getWorld();

        if (world.isRemote == false)
        {
            DataLogger.instance(world.getDimension().getType()).onChunkEvent(DataType.CHUNK_UNLOAD, chunk);
        }
    }
}