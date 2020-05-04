package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.datadump.DataDump;

public class OutputUtils
{
    public static ITextComponent getClipboardCopiableMessage(String textPre, String textToCopy, String textPost)
    {
        StringTextComponent componentCopy = new StringTextComponent(textToCopy);
        componentCopy.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellme copy-to-clipboard " + textToCopy));
        componentCopy.getStyle().setUnderlined(Boolean.TRUE);

        StringTextComponent hoverText = new StringTextComponent(String.format("Copy the string '%s' to clipboard", textToCopy));
        componentCopy.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

        StringTextComponent full = new StringTextComponent(textPre);
        full.appendSibling(componentCopy).appendText(textPost);

        return full;
    }

    public static void sendClickableLinkMessage(Entity entity, String messageKey, File file)
    {
        ITextComponent name = new StringTextComponent(file.getName());

        if (TellMe.isClient())
        {
            name.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
            name.getStyle().setUnderlined(Boolean.valueOf(true));
        }

        entity.sendMessage(new TranslationTextComponent(messageKey, name));
    }

    public static void printOutputToChat(List<String> lines, Entity entity)
    {
        for (String line : lines)
        {
            entity.sendMessage(new StringTextComponent(line));
        }
    }

    public static void printOutputToConsole(List<String> lines)
    {
        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void printOutput(@Nullable List<String> lines, OutputType outputType, DataDump.Format format,
            @Nullable String fileNameBase, Entity entity)
    {
        printOutput(lines, outputType, format, fileNameBase, entity.getCommandSource());
    }

    public static void printOutput(@Nullable List<String> lines, OutputType outputType, DataDump.Format format,
            @Nullable String fileNameBase, CommandSource source)
    {
        printOutput(lines, outputType, source, fileNameBase, format == DataDump.Format.CSV ? ".csv" : ".txt");
    }

    public static void printOutput(@Nullable List<String> lines, OutputType outputType,
            CommandSource source, @Nullable String fileNameBase, @Nullable String fileNameExtension)
    {
        if (lines == null || lines.isEmpty())
        {
            return;
        }

        @Nullable Entity entity = source.getEntity();

        switch (outputType)
        {
            case CHAT:
                if (entity != null)
                {
                    printOutputToChat(lines, entity);
                }
                break;

            case CONSOLE:
                printOutputToConsole(lines);
                entity.sendMessage(new StringTextComponent("Output printed to console"));
                break;

            case FILE:
                File file = DataDump.dumpDataToFile(fileNameBase, fileNameExtension, lines);

                if (file != null)
                {
                    if (entity != null)
                    {
                        OutputUtils.sendClickableLinkMessage(entity, "Output written to file %s", file);
                    }
                    else
                    {
                        source.sendFeedback(new StringTextComponent("Output written to file '" + file.getName() + "'"), false);
                    }
                }
                break;
        }
    }
}
