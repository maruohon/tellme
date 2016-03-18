package fi.dy.masa.tellme.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.ItemInfo;
import fi.dy.masa.tellme.util.RayTraceUtils;

public class InteractEventHandler
{
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (event.world.isRemote == true || event.entityPlayer.canCommandSenderUseCommand(4, "getblockoritemnbtinfo") == false)
        {
            return;
        }

        if (event.entityPlayer != null && event.entityPlayer.getHeldItemMainhand() != null)
        {
            // Show info for the block the player right clicks on with a gold nugget
            if (event.entityPlayer.getHeldItemMainhand().getItem() == Items.gold_nugget)
            {
                // FIXME update to new interact stuff when it is ready for 1.9
                //if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK &&
                //    event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
                if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
                {
                    return;
                }

                BlockPos pos = event.pos;

                // Ray tracing to be able to target fluid blocks, although currently it doesn't work for non-source blocks
                RayTraceResult mop = RayTraceUtils.rayTraceFromPlayer(event.world, event.entityPlayer, true);

                if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK)
                {
                    return;
                }
                // Ray traced to a block
                else
                {
                    pos = mop.getBlockPos();

                    IBlockState iBlockState = event.world.getBlockState(pos);
                    boolean isFluid = iBlockState.getBlock().getMaterial(iBlockState).isLiquid();

                    // If we ray traced to a fluid block, but the interact event is for a block (behind the fluid), then stop here
                    // Also, if the target block is not a fluid, then we don't want to do anything on the RIGHT_CLICK_AIR case, that would dupe the output
                    // FIXME update to new interact stuff when it is ready for 1.9
                    //if ((isFluid == true && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) ||
                    //    (isFluid == false && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR))
                    if (isFluid == true && event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
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
            // FIXME update to new interact stuff when it is ready for 1.9
            //else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && event.entityPlayer.getHeldItemMainhand().getItem() == Items.blaze_rod)
            else if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && event.entityPlayer.getHeldItemMainhand().getItem() == Items.blaze_rod)
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

                ItemStack stack = event.entityPlayer.inventory.getStackInSlot(slot);
                if (stack == null || stack.getItem() == null)
                {
                    return;
                }

                ItemInfo.printBasicItemInfoToChat(event.entityPlayer, stack);

                if (event.entityPlayer.isSneaking() == true)
                {
                    ItemInfo.dumpItemInfoToFile(event.entityPlayer, stack);
                }
                else
                {
                    ItemInfo.printItemInfoToConsole(stack);
                }

                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event)
    {
        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (event.entityPlayer.worldObj.isRemote == true || event.entityPlayer.canCommandSenderUseCommand(4, "getblockoritemnbtinfo") == false)
        {
            return;
        }

        if (event.entityPlayer != null && event.entityPlayer.getHeldItemMainhand() != null
            && event.getTarget() != null && event.entityPlayer.getHeldItemMainhand().getItem() == Items.gold_nugget)
        {
            EntityInfo.printBasicEntityInfoToChat(event.entityPlayer, event.getTarget());

            if (event.entityPlayer.isSneaking() == true)
            {
                EntityInfo.dumpFullEntityInfoToFile(event.entityPlayer, event.getTarget());
            }
            else
            {
                EntityInfo.printFullEntityInfoToConsole(event.entityPlayer, event.getTarget());
            }

            event.setCanceled(true);
        }
    }
}
