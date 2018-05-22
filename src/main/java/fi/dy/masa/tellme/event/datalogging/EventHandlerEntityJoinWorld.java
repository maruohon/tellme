package fi.dy.masa.tellme.event.datalogging;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class EventHandlerEntityJoinWorld
{
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();

        if (entity.getEntityWorld().isRemote == false)
        {
            DataLogger.instance(entity.getEntityWorld().provider.getDimension()).onEntityEvent(DataType.ENTITY_JOIN_WORLD, entity);
        }
    }
}
