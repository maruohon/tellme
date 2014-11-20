package fi.dy.masa.tellme.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

/*
 * Base class for handling all the commands of this mod.
 *
 * The structure of this class is based on that of CoFHCore.
 */
public class CommandTellme extends CommandBase
{
private static Map<String, CommandBase> subCommands = new HashMap<String, CommandBase>();

    @Override
    public String getCommandName()
    {
        return "tellme";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/" + this.getCommandName() + " help";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender icommandsender, String[] str)
    {
        // TODO
        return null;
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] commandArgs)
    {
        if (commandArgs.length > 0 && subCommands.containsKey(commandArgs[0]) == true)
        {
            CommandBase cb = subCommands.get(commandArgs[0]);
            if (cb != null)
            {
                cb.processCommand(icommandsender, commandArgs);
            }
            return;
        }

        throw new WrongUsageException("Type '" + getCommandUsage(icommandsender) + "' for help.");
    }

    public static void registerSubCommand(CommandBase cmd)
    {
        if (subCommands.containsKey(cmd.getCommandName()) == false)
        {
            subCommands.put(cmd.getCommandName(), cmd);
        }
    }
}
