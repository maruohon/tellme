package fi.dy.masa.tellme.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.ItemInfo;
import fi.dy.masa.tellme.util.RayTraceUtils;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InteractEventHandler
{
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        World world = event.getWorld();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (world.isRemote == true || player.canCommandSenderUseCommand(4, "getblockoritemnbtinfo") == false)
        {
            return;
        }

        if (player.getHeldItemMainhand() != null)
        {
            // Show info for the block the player right clicks on with a gold nugget
            if (player.getHeldItemMainhand().getItem() == Items.gold_nugget)
            {
                // FIXME update to new interact stuff when it is ready for 1.9
                //if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK &&
                //    event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
                if (event.getAction() != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
                {
                    return;
                }

                BlockPos pos = event.getPos();

                // Ray tracing to be able to target fluid blocks, although currently it doesn't work for non-source blocks
                RayTraceResult mop = RayTraceUtils.rayTraceFromPlayer(world, player, true);

                if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK)
                {
                    return;
                }
                // Ray traced to a block
                else
                {
                    pos = mop.getBlockPos();

                    IBlockState iBlockState = world.getBlockState(pos);
                    boolean isFluid = iBlockState.getBlock().getMaterial(iBlockState).isLiquid();

                    // If we ray traced to a fluid block, but the interact event is for a block (behind the fluid), then stop here
                    // Also, if the target block is not a fluid, then we don't want to do anything on the RIGHT_CLICK_AIR case, that would dupe the output
                    // FIXME update to new interact stuff when it is ready for 1.9
                    //if ((isFluid == true && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) ||
                    //    (isFluid == false && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR))
                    if (isFluid == true && event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
                    {
                        event.setCanceled(true);
                        return;
                    }
                }

                BlockInfo.printBasicBlockInfoToChat(player, world, pos);

                if (player.isSneaking() == true)
                {
                    BlockInfo.dumpBlockInfoToFile(player, world, pos);
                }
                else
                {
                    BlockInfo.printBlockInfoToConsole(player, world, pos);
                }

                event.setCanceled(true);
            }
            // Show info for the item to the right from the current slot when the player right clicks on air with a gold nugget
            // FIXME update to new interact stuff when it is ready for 1.9
            //else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && player.getHeldItemMainhand().getItem() == Items.blaze_rod)
            else if (event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && player.getHeldItemMainhand().getItem() == Items.blaze_rod)
            {
                // Select the slot to the right from the current slot, or the first slot if the current slot is the last slot
                int slot = player.inventory.currentItem;
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

                ItemStack stack = player.inventory.getStackInSlot(slot);
                if (stack == null || stack.getItem() == null)
                {
                    return;
                }

                ItemInfo.printBasicItemInfoToChat(player, stack);

                if (player.isSneaking() == true)
                {
                    ItemInfo.dumpItemInfoToFile(player, stack);
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
        EntityPlayer player = event.getEntityPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (player.worldObj.isRemote == true || player.canCommandSenderUseCommand(4, "getblockoritemnbtinfo") == false)
        {
            return;
        }

        if (player != null && player.getHeldItemMainhand() != null
            && event.getTarget() != null && player.getHeldItemMainhand().getItem() == Items.gold_nugget)
        {
            EntityInfo.printBasicEntityInfoToChat(player, event.getTarget());

            if (player.isSneaking() == true)
            {
                EntityInfo.dumpFullEntityInfoToFile(player, event.getTarget());
            }
            else
            {
                EntityInfo.printFullEntityInfoToConsole(player, event.getTarget());
            }

            event.setCanceled(true);
        }
    }
}
