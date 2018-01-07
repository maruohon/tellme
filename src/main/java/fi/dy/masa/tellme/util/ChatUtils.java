package fi.dy.masa.tellme.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class ChatUtils
{
    public static ITextComponent getClipboardCopiableMessage(String textPre, String textToCopy, String textPost)
    {
        TextComponentString componentCopy = new TextComponentString(textToCopy);
        componentCopy.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellme copy-to-clipboard " + textToCopy));
        componentCopy.getStyle().setUnderlined(Boolean.TRUE);

        TextComponentString hoverText = new TextComponentString(String.format("Copy the string '%s' to clipboard", textToCopy));
        componentCopy.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

        TextComponentString full = new TextComponentString(textPre);
        full.appendSibling(componentCopy).appendText(textPost);

        return full;
    }
}
