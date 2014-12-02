package fi.dy.masa.tellme.event;

import net.minecraft.init.Items;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;

public class InteractEventHandler
{
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.world.isRemote == true)
        {
            return;
        }

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entityPlayer != null
            && event.entityPlayer.getCurrentEquippedItem() != null
            && event.entityPlayer.getCurrentEquippedItem().getItem() == Items.gold_nugget)
        {
            BlockInfo.printBasicBlockInfoToChat(event.entityPlayer, event.world, event.x, event.y, event.z);

            if (event.entityPlayer.isSneaking() == true)
            {
                BlockInfo.dumpBlockInfoToFile(event.entityPlayer, event.world, event.x, event.y, event.z);
            }
            else
            {
                BlockInfo.printBlockInfoToConsole(event.entityPlayer, event.world, event.x, event.y, event.z);
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event)
    {
        if (event.entityPlayer.worldObj.isRemote == true)
        {
            return;
        }

        if (event.entityPlayer != null && event.entityPlayer.getCurrentEquippedItem() != null
            && event.target != null && event.entityPlayer.getCurrentEquippedItem().getItem() == Items.gold_nugget)
        {
            EntityInfo.printBasicEntityInfoToChat(event.entityPlayer, event.target);

            if (event.entityPlayer.isSneaking() == true)
            {
                EntityInfo.dumpEntityInfoToFile(event.entityPlayer, event.target);
            }
            else
            {
                EntityInfo.printEntityInfoToConsole(event.entityPlayer, event.target);
            }

            event.setCanceled(true);
        }
    }
}
