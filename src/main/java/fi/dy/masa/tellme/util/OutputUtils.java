package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class OutputUtils
{
    public static Text getClipboardCopiableMessage(String textPre, String textToCopy, String textPost)
    {
        return getClipboardCopiableMessage(new LiteralText(textPre), new LiteralText(textToCopy), new LiteralText(textPost));
    }

    public static MutableText getClipboardCopiableMessage(MutableText textPre, MutableText textToCopy, MutableText textPost)
    {
        final String copyString = textToCopy.getString();
        textToCopy.styled((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellme copy-to-clipboard " + copyString)));
        textToCopy.formatted(Formatting.UNDERLINE);

        LiteralText hoverText = new LiteralText(String.format("Copy the string '%s' to clipboard", textToCopy.getString()));
        textToCopy.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

        return textPre.append(textToCopy).append(textPost);
    }

    public static void sendClickableLinkMessage(PlayerEntity player, String messageKey, final File file)
    {
        LiteralText name = new LiteralText(file.getName());

        if (TellMe.isClient())
        {
            name.styled((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
            name.formatted(Formatting.UNDERLINE);
        }

        player.sendMessage(new TranslatableText(messageKey, name), false);
    }

    public static void printOutputToChat(List<String> lines, PlayerEntity entity)
    {
        for (String line : lines)
        {
            entity.sendMessage(new LiteralText(line), false);
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
                    player.sendMessage(new LiteralText("Output printed to console"), false);
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
                        source.sendFeedback(new LiteralText("Output written to file '" + file.getName() + "'"), false);
                    }
                }
                break;
        }
    }
}
