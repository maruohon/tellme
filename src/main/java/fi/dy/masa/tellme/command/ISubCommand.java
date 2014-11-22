package fi.dy.masa.tellme.command;

import java.util.List;

import net.minecraft.command.ICommandSender;

public interface ISubCommand
{
    /* Returns the command name */
    String getCommandName();

    /* Processes the command */
    void processCommand(ICommandSender sender, String[] args);

    /* Adds the tab completion options */
    List<String> addTabCompletionOptions(ICommandSender sender, String[] args);
}
