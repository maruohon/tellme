package fi.dy.masa.tellme.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/*
 * Base class for handling all the commands of this mod.
 *
 * The structure of this class is based on that of CoFHCore.
 */
public class CommandTellme extends CommandBase
{
    public static CommandTellme instance = new CommandTellme();
    private static Map<String, ISubCommand> subCommands = new HashMap<String, ISubCommand>();

    static
    {
        registerSubCommand(new SubCommandBiome());
        registerSubCommand(new SubCommandBlockStats());
        registerSubCommand(new SubCommandHelp());
    }

    @Override
    public String getName()
    {
        return "tellme";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/" + this.getName() + " help";
    }

    @Override
    public boolean canCommandSenderUse(ICommandSender icommandsender)
    {
        return icommandsender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender icommandsender, String[] strArr, BlockPos pos)
    {
        if (strArr.length == 1)
        {
            //return getListOfStringsFromIterableMatchingLastWord(strArr, subCommands.keySet());
            return func_175762_a(strArr, subCommands.keySet());
        }
        else if (subCommands.containsKey(strArr[0]))
        {
            ISubCommand sc = subCommands.get(strArr[0]);
            if (sc != null)
            {
                return sc.addTabCompletionOptions(icommandsender, strArr);
            }
        }
        return null;
    }

    @Override
    public void execute(ICommandSender icommandsender, String[] commandArgs) throws CommandException
    {
        if (commandArgs.length > 0)
        {
            if (subCommands.containsKey(commandArgs[0]) == true)
            {
                ISubCommand cb = subCommands.get(commandArgs[0]);
                if (cb != null)
                {
                    cb.processCommand(icommandsender, commandArgs);
                    return;
                }
            }
            else
            {
                throw new WrongUsageException(StatCollector.translateToLocal("info.command.unknown") + ": /" + this.getName() + " " + commandArgs[0], new Object[0]);
            }
        }

        throw new WrongUsageException(StatCollector.translateToLocal("info.command.help") + ": '" + getCommandUsage(icommandsender) + "'", new Object[0]);
    }

    public static void registerSubCommand(ISubCommand cmd)
    {
        if (subCommands.containsKey(cmd.getCommandName()) == false)
        {
            subCommands.put(cmd.getCommandName(), cmd);
        }
    }

    public static Set<String> getSubCommandList()
    {
        return subCommands.keySet();
    }

    public static void registerCommand(FMLServerStartingEvent event)
    {
        event.registerServerCommand(instance);
    }
}
