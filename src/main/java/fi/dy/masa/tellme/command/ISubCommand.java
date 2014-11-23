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

    /* Gets the sub commands for this (sub) command.*/
    List<String> getSubCommands();

    /* Gets the sub command help string ready for printing. */
    String getSubCommandsHelpString();
}
