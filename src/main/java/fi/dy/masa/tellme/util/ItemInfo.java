package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.nbt.NbtStringifierPretty;

public class ItemInfo
{
    public static boolean areItemStacksEqual(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2)
    {
        if (stack1.isEmpty() || stack2.isEmpty())
        {
            return stack1.isEmpty() == stack2.isEmpty();
        }

        return stack1.isItemEqual(stack2) && ItemStack.areTagsEqual(stack1, stack2);
    }

    private static List<String> getFullItemInfo(@Nonnull ItemStack stack)
    {
        if (stack.hasTag() == false)
        {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();

        lines.add(ItemData.getFor(stack).toString());
        lines.add("");
        lines.add(stack.getTag().toString());
        lines.add("");

        lines.addAll((new NbtStringifierPretty(null)).getNbtLines(stack.getTag()));

        return lines;
    }

    private static List<String> getPrettyNbtForChat(@Nonnull ItemStack stack)
    {
        if (stack.hasTag() == false)
        {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.addAll((new NbtStringifierPretty(Formatting.GRAY.toString())).getNbtLines(stack.getTag()));

        return lines;
    }

    public static void printBasicItemInfoToChat(PlayerEntity entity, @Nonnull ItemStack stack)
    {
        entity.sendMessage(ItemData.getFor(stack).toChatMessage(), false);
    }

    public static void printItemInfo(PlayerEntity entity, @Nonnull ItemStack stack, OutputType outputType)
    {
        printBasicItemInfoToChat(entity, stack);

        List<String> lines;

        if (outputType == OutputType.CHAT && stack.hasTag())
        {
            entity.sendMessage(OutputUtils.getClipboardCopiableMessage("", stack.getTag().toString(), ""), false);
            lines = getPrettyNbtForChat(stack);
        }
        else
        {
            lines = getFullItemInfo(stack);
        }

        OutputUtils.printOutput(lines, outputType, DataDump.Format.ASCII, "item_data", entity);
    }

    public static class ItemData
    {
        private final String regName;
        private final int id;
        private final String displayName;
        private final String nbtInfo;

        public ItemData(String displayName, String regName, int id, String nbtInfo)
        {
            this.displayName = displayName;
            this.regName = regName;
            this.id = id;
            this.nbtInfo = nbtInfo;
        }

        public static ItemData getFor(ItemStack stack)
        {
            String registryName = Registry.ITEM.getId(stack.getItem()).toString();
            String nbtInfo;

            if (stack.hasTag())
            {
                nbtInfo = "has NBT data";
            }
            else
            {
                nbtInfo = "no NBT data";
            }

            return new ItemData(stack.getName().getString(), registryName, Item.getRawId(stack.getItem()), nbtInfo);
        }

        public Text toChatMessage()
        {
            String textPre = String.format("%s (", this.displayName);
            String textPost = String.format(" - id: %d) %s", this.id, this.nbtInfo);

            return OutputUtils.getClipboardCopiableMessage(textPre, this.regName, textPost);
        }

        @Override
        public String toString()
        {
            return String.format("%s (%s - id: %d) %s", this.displayName, this.regName, this.id, this.nbtInfo);
        }
    }
}
