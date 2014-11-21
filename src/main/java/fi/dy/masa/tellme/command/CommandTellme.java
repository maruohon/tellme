package fi.dy.masa.tellme.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

/*
 * Base class for handling all the commands of this mod.
 *
 * The structure of this class is based on that of CoFHCore.
 */
public class CommandTellme extends CommandBase
{
    public static CommandTellme instance = new CommandTellme();
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
    public boolean canCommandSenderUseCommand(ICommandSender icommandsender)
    {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender icommandsender, String[] strArr)
    {
        if (strArr.length == 1)
        {
            return getListOfStringsFromIterableMatchingLastWord(strArr, subCommands.keySet());
        }
        else if (subCommands.containsKey(strArr[0]))
        {
            CommandBase sc = subCommands.get(strArr[0]);
            if (sc != null)
            {
                return sc.addTabCompletionOptions(icommandsender, strArr);
            }
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] commandArgs)
    {
        if (commandArgs.length > 0)
        {
            if (subCommands.containsKey(commandArgs[0]) == true)
            {
                CommandBase cb = subCommands.get(commandArgs[0]);
                if (cb != null)
                {
                    cb.processCommand(icommandsender, commandArgs);
                    return;
                }
            }
            else
            {
                /*if (icommandsender instanceof EntityPlayer)
                {
                    System.out.println("remote: " + ((EntityPlayer)icommandsender).worldObj.isRemote);
                }*/
                throw new WrongUsageException("Unrecognized command: " + "/" + this.getCommandName() + " " + commandArgs[0]);
            }
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

    public static void registerCommand(FMLServerStartingEvent event)
    {
        event.registerServerCommand(instance);
    }
}
