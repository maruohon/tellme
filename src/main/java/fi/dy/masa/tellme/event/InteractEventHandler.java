package fi.dy.masa.tellme.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.ItemInfo;
import fi.dy.masa.tellme.util.RayTraceUtils;

public class InteractEventHandler
{
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        EntityPlayer player = event.getEntityPlayer();
        World world = event.getWorld();
        ItemStack stack = player.getHeldItemMainhand();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (world.isRemote == true || stack == null || stack.getItem() != Items.GOLD_NUGGET || event.getHand() != EnumHand.MAIN_HAND ||
            player.canUseCommand(4, "getblockoritemnbtinfo") == false)
        {
            return;
        }

        this.printBlockInfo(world, player);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event)
    {
        EntityPlayer player = event.getEntityPlayer();
        World world = event.getWorld();
        ItemStack stack = player.getHeldItemMainhand();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (world.isRemote == true || stack == null || event.getHand() != EnumHand.MAIN_HAND ||
            player.canUseCommand(4, "getblockoritemnbtinfo") == false)
        {
            return;
        }

        if (stack.getItem() == Items.BLAZE_ROD)
        {
            this.printItemInfo(event.getEntityPlayer());
            //event.setCanceled(true);
        }
        // Block info for fluid blocks without clicking on a block behind the fluid
        else if (stack.getItem() == Items.GOLD_NUGGET)
        {
            //this.printBlockInfo(world, player);
            //event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItemMainhand();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (player.getEntityWorld().isRemote == true || stack == null || stack.getItem() != Items.GOLD_NUGGET ||
            event.getHand() != EnumHand.MAIN_HAND || player.canUseCommand(4, "getblockoritemnbtinfo") == false)
        {
            return;
        }

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

    private void printBlockInfo(World world, EntityPlayer player)
    {
        // Ray tracing to be able to target fluid blocks, although currently it doesn't work for non-source blocks
        RayTraceResult mop = RayTraceUtils.rayTraceFromPlayer(world, player, true);
        BlockPos pos;

        if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK)
        {
            return;
        }
        // Ray traced to a block
        else
        {
            pos = mop.getBlockPos();

            //IBlockState iBlockState = world.getBlockState(pos);
            //boolean isFluid = iBlockState.getBlock().getMaterial(iBlockState).isLiquid();

            // If we ray traced to a fluid block, but the interact event is for a block
            // (behind the fluid), then stop here.
            // Also, if the target block is not a fluid, then we don't want to do anything
            // on the RIGHT_CLICK_AIR case, as that would dupe the output.
            // FIXME update to new interact stuff when it is ready for 1.9
            //if ((isFluid == true && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) ||
            //    (isFluid == false && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR))
            /*if (isFluid == true && event.getAction() == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)
            {
                return;
            }*/
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
    }

    private void printItemInfo(EntityPlayer player)
    {
        // Select the slot to the right from the current slot, or the first slot if the current slot is the last slot
        int slot = player.inventory.currentItem;
        if (slot >= 0 && slot <= 7)
        {
            slot += 1;
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
    }
}
