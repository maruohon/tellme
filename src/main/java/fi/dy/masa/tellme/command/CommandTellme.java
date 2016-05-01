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

/*
 * Base class for handling all the commands of this mod.
 *
 * The structure of this class is based on that of CoFHCore.
 */
public class CommandTellme extends CommandBase
{
    private final Map<String, ISubCommand> subCommands = new HashMap<String, ISubCommand>();

    public CommandTellme()
    {
        this.registerSubCommand(new SubCommandBiome(this));
        this.registerSubCommand(new SubCommandBlockStats(this));
        this.registerSubCommand(new SubCommandDump(this));
        this.registerSubCommand(new SubCommandHelp(this));
    }

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
        return 4;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender icommandsender, String[] strArr, BlockPos pos)
    {
        if (strArr.length == 1)
        {
            return getListOfStringsMatchingLastWord(strArr, this.subCommands.keySet());
        }
        else if (this.subCommands.containsKey(strArr[0]))
        {
            ISubCommand sc = this.subCommands.get(strArr[0]);
            if (sc != null)
            {
                return sc.addTabCompletionOptions(icommandsender, strArr);
            }
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] commandArgs) throws CommandException
    {
        if (commandArgs.length > 0)
        {
            if (this.subCommands.containsKey(commandArgs[0]) == true)
            {
                ISubCommand cb = this.subCommands.get(commandArgs[0]);
                if (cb != null)
                {
                    cb.processCommand(icommandsender, commandArgs);
                    return;
                }
            }
            else
            {
                throw new WrongUsageException(StatCollector.translateToLocal("info.command.unknown") + ": /" + this.getCommandName() + " " + commandArgs[0], new Object[0]);
            }
        }

        throw new WrongUsageException(StatCollector.translateToLocal("info.command.help") + ": '" + getCommandUsage(icommandsender) + "'", new Object[0]);
    }

    public void registerSubCommand(ISubCommand cmd)
    {
        if (this.subCommands.containsKey(cmd.getCommandName()) == false)
        {
            this.subCommands.put(cmd.getCommandName(), cmd);
        }
    }

    public Set<String> getSubCommandList()
    {
        return this.subCommands.keySet();
    }
}
