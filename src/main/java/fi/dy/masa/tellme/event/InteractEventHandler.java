package fi.dy.masa.tellme.event;

import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.ItemInfo;
import fi.dy.masa.tellme.util.MOPHelper;

public class InteractEventHandler
{
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (event.world.isRemote == true || event.entityPlayer.canUseCommand(4, "getblockoritemnbtinfo") == false)
        {
            return;
        }

        if (event.entityPlayer != null && event.entityPlayer.getCurrentEquippedItem() != null)
        {
            // Show info for the block the player right clicks on with a gold nugget
            if (event.entityPlayer.getCurrentEquippedItem().getItem() == Items.gold_nugget)
            {
                if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
                    && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
                {
                    return;
                }

                BlockPos pos = event.pos;

                // Ray tracing to be able to target fluid blocks, although currently it doesn't work for non-source blocks
                MovingObjectPosition mop = MOPHelper.getMovingObjectPositionFromPlayer(event.world, event.entityPlayer, true);

                if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK)
                {
                    return;
                }
                // Ray traced to a block
                else
                {
                    pos = mop.getBlockPos();

                    boolean isFluid = event.world.getBlockState(pos).getBlock().getMaterial().isLiquid();

                    // If we ray traced to a fluid block, but the interact event is for a block (behind the fluid), then stop here
                    // Also, if the target block is not a fluid, then we don't want to do anything on the RIGHT_CLICK_AIR case, that would dupe the output
                    if ((isFluid == true && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
                        || (isFluid == false && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR))
                    {
                        event.setCanceled(true);
                        return;
                    }
                }

                BlockInfo.printBasicBlockInfoToChat(event.entityPlayer, event.world, pos);

                if (event.entityPlayer.isSneaking() == true)
                {
                    BlockInfo.dumpBlockInfoToFile(event.entityPlayer, event.world, pos);
                }
                else
                {
                    BlockInfo.printBlockInfoToConsole(event.entityPlayer, event.world, pos);
                }

                event.setCanceled(true);
            }
            // Show info for the item to the right from the current slot when the player right clicks on air with a gold nugget
            else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && event.entityPlayer.getCurrentEquippedItem().getItem() == Items.blaze_rod)
            {
                // Select the slot to the right from the current slot, or the first slot if the current slot is the last slot
                int slot = event.entityPlayer.inventory.currentItem;
                if (slot >= 0 && slot <= 7)
                {
                    slot++;
                }
                else if (slot == 8)
                {
                    slot = 0;
                }
                else
                {
                    return;
                }

                if (event.entityPlayer.inventory.getStackInSlot(slot) == null)
                {
                    return;
                }

                ItemInfo.printBasicItemInfoToChat(event.entityPlayer, slot);

                if (event.entityPlayer.isSneaking() == true)
                {
                    ItemInfo.dumpItemInfoToFile(event.entityPlayer, slot);
                }
                else
                {
                    ItemInfo.printItemInfoToConsole(event.entityPlayer, slot);
                }

                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event)
    {
        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (event.entityPlayer.worldObj.isRemote == true || event.entityPlayer.canUseCommand(4, "getblockoritemnbtinfo") == false)
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
