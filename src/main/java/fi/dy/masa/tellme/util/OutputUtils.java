package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class OutputUtils
{
    public static Component getClipboardCopiableMessage(String textPre, String textToCopy, String textPost)
    {
        return getClipboardCopiableMessage(new TextComponent(textPre), new TextComponent(textToCopy), new TextComponent(textPost));
    }

    public static MutableComponent getClipboardCopiableMessage(MutableComponent textPre, MutableComponent textToCopy, MutableComponent textPost)
    {
        final String copyString = textToCopy.getString();
        textToCopy.withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellme copy-to-clipboard " + copyString)));
        textToCopy.withStyle(ChatFormatting.UNDERLINE);

        TextComponent hoverText = new TextComponent(String.format("Copy the string '%s' to clipboard", textToCopy.getString()));
        textToCopy.getStyle().withHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverText));

        return textPre.append(textToCopy).append(textPost);
    }

    public static void sendClickableLinkMessage(Player player, String messageKey, final File file)
    {
        TextComponent name = new TextComponent(file.getName());

        if (TellMe.isClient())
        {
            name.withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
            name.withStyle(ChatFormatting.UNDERLINE);
        }

        player.displayClientMessage(new TranslatableComponent(messageKey, name), false);
    }

    public static void printOutputToChat(List<String> lines, Player entity)
    {
        for (String line : lines)
        {
            entity.displayClientMessage(new TextComponent(line), false);
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
        printOutput(lines, outputType, format, fileNameBase, entity.createCommandSourceStack());
    }

    public static void printOutput(@Nullable List<String> lines, OutputType outputType, DataDump.Format format,
            @Nullable String fileNameBase, CommandSourceStack source)
    {
        printOutput(lines, outputType, source, fileNameBase, format == DataDump.Format.CSV ? ".csv" : ".txt");
    }

    public static void printOutput(@Nullable List<String> lines, OutputType outputType,
            CommandSourceStack source, @Nullable String fileNameBase, @Nullable String fileNameExtension)
    {
        if (lines == null || lines.isEmpty())
        {
            return;
        }

        @Nullable Player player = source.getEntity() instanceof Player ? (Player) source.getEntity() : null;

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
                    player.displayClientMessage(new TextComponent("Output printed to console"), false);
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
                        source.sendSuccess(new TextComponent("Output written to file '" + file.getName() + "'"), false);
                    }
                }
                break;
        }
    }
}
