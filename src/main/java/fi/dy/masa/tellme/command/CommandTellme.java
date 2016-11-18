package fi.dy.masa.tellme.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

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
        this.registerSubCommand(new SubCommandLoaded(this));
    }

    @Override
    public String getName()
    {
        return "tellme";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/" + this.getName() + " help";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] strArr, BlockPos pos)
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
                return sc.getTabCompletions(server, sender, strArr);
            }
        }
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] commandArgs) throws CommandException
    {
        if (commandArgs.length > 0)
        {
            if (this.subCommands.containsKey(commandArgs[0]) == true)
            {
                ISubCommand cb = this.subCommands.get(commandArgs[0]);
                if (cb != null)
                {
                    cb.execute(server, sender, commandArgs);
                    return;
                }
            }
            else
            {
                throw new WrongUsageException(I18n.translateToLocal("info.command.unknown") + ": /" + this.getName() + " " + commandArgs[0], new Object[0]);
            }
        }

        throw new WrongUsageException(I18n.translateToLocal("info.command.help") + ": '" + getUsage(sender) + "'", new Object[0]);
    }

    public void registerSubCommand(ISubCommand cmd)
    {
        if (this.subCommands.containsKey(cmd.getName()) == false)
        {
            this.subCommands.put(cmd.getName(), cmd);
        }
    }

    public Set<String> getSubCommandList()
    {
        return this.subCommands.keySet();
    }
}
