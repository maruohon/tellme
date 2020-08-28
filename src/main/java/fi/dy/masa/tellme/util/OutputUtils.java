package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class OutputUtils
{
    public static ITextComponent getClipboardCopiableMessage(String textPre, String textToCopy, String textPost)
    {
        return getClipboardCopiableMessage(new StringTextComponent(textPre), new StringTextComponent(textToCopy), new StringTextComponent(textPost));
    }

    public static IFormattableTextComponent getClipboardCopiableMessage(IFormattableTextComponent textPre, IFormattableTextComponent textToCopy, IFormattableTextComponent textPost)
    {
        final String copyString = textToCopy.getString();
        textToCopy.modifyStyle((style) -> style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellme copy-to-clipboard " + copyString)));
        textToCopy.mergeStyle(TextFormatting.UNDERLINE);

        StringTextComponent hoverText = new StringTextComponent(String.format("Copy the string '%s' to clipboard", textToCopy.getString()));
        textToCopy.getStyle().setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverText));

        return textPre.append(textToCopy).append(textPost);
    }

    public static void sendClickableLinkMessage(PlayerEntity player, String messageKey, final File file)
    {
        StringTextComponent name = new StringTextComponent(file.getName());

        if (TellMe.isClient())
        {
            name.modifyStyle((style) -> style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
            name.mergeStyle(TextFormatting.UNDERLINE);
        }

        player.sendStatusMessage(new TranslationTextComponent(messageKey, name), false);
    }

    public static void printOutputToChat(List<String> lines, PlayerEntity entity)
    {
        for (String line : lines)
        {
            entity.sendStatusMessage(new StringTextComponent(line), false);
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

        @Nullable PlayerEntity player = source.getEntity() instanceof PlayerEntity ? (PlayerEntity) source.getEntity() : null;

        switch (outputType)
        {
            case CHAT:
                if (player != null)
                {
                    printOutputToChat(lines, player);
                }
                break;

            case CONSOLE:
                printOutputToConsole(lines);

                if (player != null)
                {
                    player.sendStatusMessage(new StringTextComponent("Output printed to console"), false);
                }
                break;

            case FILE:
                File file = DataDump.dumpDataToFile(fileNameBase, fileNameExtension, lines);

                if (file != null)
                {
                    if (player != null)
                    {
                        OutputUtils.sendClickableLinkMessage(player, "Output written to file %s", file);
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
