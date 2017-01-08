package fi.dy.masa.tellme.event;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.tellme.config.Configs;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.ItemInfo;

public class InteractEventHandler
{
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        this.printBlockInfo(event);
    }

    @SubscribeEvent
    public void onRightClickAir(PlayerInteractEvent.RightClickEmpty event)
    {
        this.printBlockInfo(event);
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event)
    {
        EntityPlayer player = event.getEntityPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.enableDebugItemForItems && event.getWorld().isRemote == false &&
            event.getHand() == EnumHand.MAIN_HAND && player.canUseCommand(4, "tellme"))
        {
            if (areItemStacksEqual(Configs.debugItemItems, player.getHeldItemMainhand()))
            {
                this.printItemInfo(event.getEntityPlayer());
            }
            /*
            else if (areItemStacksEqual(Configs.debugItemBlocks, player.getHeldItemMainhand()))
            {
              this.printBlockInfo(world, player);
              event.setCanceled(true);
            }
            */
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event)
    {
        EntityPlayer player = event.getEntityPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.enableDebugItemForBlockAndEntities && event.getWorld().isRemote == false && event.getHand() == EnumHand.MAIN_HAND &&
            player.canUseCommand(4, "tellme") && areItemStacksEqual(Configs.debugItemBlocks, player.getHeldItemMainhand()))
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

    private void printBlockInfo(PlayerInteractEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();

        // The command name isn't important, only that it doesn't match the vanilla allowed-for-everyone commands
        if (Configs.enableDebugItemForBlockAndEntities && event.getWorld().isRemote == false && event.getHand() == EnumHand.MAIN_HAND &&
            player.canUseCommand(4, "tellme") && areItemStacksEqual(Configs.debugItemBlocks, player.getHeldItemMainhand()))
        {
            BlockInfo.getBlockInfoFromRayTracedTarget(event.getWorld(), player);
            event.setCanceled(true);
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

        if (player.isSneaking())
        {
            ItemInfo.dumpItemInfoToFile(player, stack);
        }
        else
        {
            ItemInfo.printItemInfoToConsole(stack);
        }
    }

    public static boolean areItemStacksEqual(@Nullable ItemStack stack1, @Nullable ItemStack stack2)
    {
        if (stack1 == null || stack2 == null)
        {
            return stack1 == stack2;
        }

        return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }
}
