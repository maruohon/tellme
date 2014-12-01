package fi.dy.masa.tellme.util;

import java.io.File;

import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class ChatUtils
{
    public static IChatComponent getClickableFileLink(File file, String message)
    {
        if (file == null)
        {
            throw new WrongUsageException("info.command.output.to.file.failed");
        }

        ChatComponentText text = new ChatComponentText(file.getName());
        text.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
        text.getChatStyle().setUnderlined(Boolean.valueOf(true));

        return new ChatComponentTranslation(message, new Object[] {text});
    }
}
