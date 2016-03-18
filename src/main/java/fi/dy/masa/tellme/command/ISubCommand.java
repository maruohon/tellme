package fi.dy.masa.tellme.command;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public interface ISubCommand
{
    /* Returns the command name */
    String getCommandName();

    /* Processes the command */
    void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException;

    /* Adds the tab completion options */
    List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args);

    /* Gets the sub commands for this (sub) command.*/
    List<String> getSubCommands();

    /* Gets the sub command help string ready for printing. */
    String getSubCommandsHelpString();
}
