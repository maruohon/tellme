package fi.dy.masa.tellme.event.datalogging;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventHandlerEntityJoinWorld
{
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        DataLogger.instance().onEntityEvent(DataType.ENTITY_JOIN_WORLD, event.getEntity());
    }
}
