package fi.dy.masa.tellme.event;

import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.config.Configs;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.ItemInfo;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.RayTraceUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

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
        Player player = event.getPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.Generic.enableDebugItemForItems && event.getWorld().isClientSide == false &&
            event.getHand() == InteractionHand.MAIN_HAND && player.createCommandSourceStack().hasPermission(4))
        {
            if (ItemInfo.areItemStacksEqual(Configs.debugItemItems, player.getMainHandItem()))
            {
                this.printItemInfo(player);
            }
            else if (ItemInfo.areItemStacksEqual(Configs.debugItemBlocks, player.getMainHandItem()))
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
        Player player = event.getPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.Generic.enableDebugItemForBlocksAndEntities &&
            event.getHand() == InteractionHand.MAIN_HAND &&
            player.createCommandSourceStack().hasPermission(4) &&
            ItemStack.isSame(Configs.debugItemBlocks, player.getMainHandItem()))
        {
            if (event.getWorld().isClientSide == false)
            {
                if (player.isShiftKeyDown())
                {
                    EntityInfo.dumpFullEntityInfoToFile(player, entity);
                }
                else
                {
                    EntityInfo.printFullEntityInfoToConsole(player, entity);
                }
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private void printBlockInfo(PlayerInteractEvent event, boolean useLiquids)
    {
        Player player = event.getPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.Generic.enableDebugItemForBlocksAndEntities &&
            event.getHand() == InteractionHand.MAIN_HAND &&
            player.createCommandSourceStack().hasPermission(4) &&
            ItemStack.isSame(Configs.debugItemBlocks, player.getMainHandItem()))
        {
            if (event.getWorld().isClientSide == false)
            {
                HitResult trace = RayTraceUtils.getRayTraceFromEntity(event.getWorld(), player, useLiquids);
                boolean adjacent = ItemInfo.areItemStacksEqual(Configs.debugItemBlocks, player.getOffhandItem());
                List<String> lines = BlockInfo.getBlockInfoFromRayTracedTarget(event.getWorld(), player, trace, adjacent, false);
                OutputType outputType = player.isShiftKeyDown() ? OutputType.FILE : OutputType.CONSOLE;

                OutputUtils.printOutput(lines, outputType, DataDump.Format.ASCII, "block_info_", player);
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private void printItemInfo(Player player)
    {
        // Select the slot to the right from the current slot, or the first slot if the current slot is the last slot
        int slot = player.inventory.selected;
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

        ItemStack stack = player.inventory.getItem(slot);

        if (stack.isEmpty() == false && stack.getItem() != null)
        {
            ItemInfo.printItemInfo(player, stack, player.isShiftKeyDown() ? OutputType.FILE : OutputType.CONSOLE);
        }
    }
}
