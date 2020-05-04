package fi.dy.masa.tellme.event.datalogging;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandlerEntityJoinWorld
{
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getEntityWorld();

        if (world.isRemote == false)
        {
            DataLogger.instance(world.getDimension().getType()).onEntityEvent(DataType.ENTITY_JOIN_WORLD, entity);
        }
    }
}
