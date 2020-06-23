package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class OutputUtils
{
    public static Text getClipboardCopiableMessage(String textPre, String textToCopy, String textPost)
    {
        return getClipboardCopiableMessage(new LiteralText(textPre), new LiteralText(textToCopy), new LiteralText(textPost));
    }

    public static Text getClipboardCopiableMessage(Text textPre, Text textToCopy, Text textPost)
    {
        textToCopy.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellme copy-to-clipboard " + textToCopy.getString()));
        textToCopy.getStyle().setUnderline(Boolean.TRUE);

        LiteralText hoverText = new LiteralText(String.format("Copy the string '%s' to clipboard", textToCopy.getString()));
        textToCopy.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

        return textPre.append(textToCopy).append(textPost);
    }

    public static void sendClickableLinkMessage(Entity entity, String messageKey, File file)
    {
        Text name = new LiteralText(file.getName());

        if (TellMe.isClient())
        {
            name.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
            name.getStyle().setUnderline(Boolean.TRUE);
        }

        entity.sendMessage(new TranslatableText(messageKey, name));
    }

    public static void printOutputToChat(List<String> lines, Entity entity)
    {
        for (String line : lines)
        {
            entity.sendMessage(new LiteralText(line));
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
            @Nullable String fileNameBase, ServerCommandSource source)
    {
        printOutput(lines, outputType, source, fileNameBase, format == DataDump.Format.CSV ? ".csv" : ".txt");
    }

    public static void printOutput(@Nullable List<String> lines, OutputType outputType,
            ServerCommandSource source, @Nullable String fileNameBase, @Nullable String fileNameExtension)
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

                if (entity != null)
                {
                    entity.sendMessage(new LiteralText("Output printed to console"));
                }
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
                        source.sendFeedback(new LiteralText("Output written to file '" + file.getName() + "'"), false);
                    }
                }
                break;
        }
    }
}
