package fi.dy.masa.tellme.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.config.Configs;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.ItemInfo;
import fi.dy.masa.tellme.util.RayTraceUtils;

public class InteractEventHandler
{
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        this.printBlockInfo(event, false);
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event)
    {
        EntityPlayer player = event.getEntityPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.enableDebugItemForItems && event.getWorld().isRemote == false &&
            event.getHand() == EnumHand.MAIN_HAND && player.canUseCommand(4, "tellme"))
        {
            if (ItemInfo.areItemStacksEqual(Configs.debugItemItems, player.getHeldItemMainhand()))
            {
                this.printItemInfo(event.getEntityPlayer());
            }
            else if (ItemInfo.areItemStacksEqual(Configs.debugItemBlocks, player.getHeldItemMainhand()))
            {
                this.printBlockInfo(event, true);
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event)
    {
        this.printEntityInfo(event, event.getTarget());
    }

    @SubscribeEvent
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event)
    {
        this.printEntityInfo(event, event.getTarget());
    }

    private void printEntityInfo(PlayerInteractEvent event, Entity entity)
    {
        EntityPlayer player = event.getEntityPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.enableDebugItemForBlockAndEntities &&
            event.getHand() == EnumHand.MAIN_HAND &&
            player.canUseCommand(4, "tellme") &&
            ItemInfo.areItemStacksEqual(Configs.debugItemBlocks, player.getHeldItemMainhand()))
        {
            if (event.getWorld().isRemote == false)
            {
                EntityInfo.printEntityInfo(player, entity, player.isSneaking());
            }

            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }

    private void printBlockInfo(PlayerInteractEvent event, boolean useLiquids)
    {
        EntityPlayer player = event.getEntityPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.enableDebugItemForBlockAndEntities &&
            event.getHand() == EnumHand.MAIN_HAND &&
            player.canUseCommand(4, "tellme") &&
            ItemInfo.areItemStacksEqual(Configs.debugItemBlocks, player.getHeldItemMainhand()))
        {
            if (event.getWorld().isRemote == false)
            {
                BlockInfo.getBlockInfoFromRayTracedTarget(event.getWorld(), player,
                        RayTraceUtils.getRayTraceFromEntity(event.getWorld(), player, useLiquids),
                        ItemInfo.areItemStacksEqual(Configs.debugItemBlocks, player.getHeldItemOffhand()));
            }

            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
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

        if (stack.isEmpty() == false && stack.getItem() != null)
        {
            ItemInfo.printItemInfo(player, stack, player.isSneaking());
        }
    }
}
