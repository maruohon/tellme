package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandTellme extends CommandBase
{
    private final Map<String, ISubCommand> subCommands = new HashMap<String, ISubCommand>();

    public CommandTellme()
    {
        this.registerSubCommand(new SubCommandBiome(this));
        this.registerSubCommand(new SubCommandBlockStats(this));
        this.registerSubCommand(new SubCommandDump(this));
        this.registerSubCommand(new SubCommandHelp(this));
        this.registerSubCommand(new SubCommandHolding(this));
        this.registerSubCommand(new SubCommandList(this));
        this.registerSubCommand(new SubCommandLoaded(this));
        this.registerSubCommand(new SubCommandLookingAt(this));
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
                ISubCommand cmd = this.subCommands.get(commandArgs[0]);

                if (cmd != null)
                {
                    cmd.execute(server, sender, commandArgs);
                    return;
                }
            }
            else
            {
                throw new WrongUsageException("tellme.command.error.unknown", "/" + this.getName() + " " + commandArgs[0]);
            }
        }

        throw new WrongUsageException("tellme.command.info.help", getUsage(sender));
    }

    public void registerSubCommand(ISubCommand cmd)
    {
        if (this.subCommands.containsKey(cmd.getName()) == false)
        {
            this.subCommands.put(cmd.getName(), cmd);
        }
    }

    public List<String> getSubCommandList()
    {
        List<String> cmds = new ArrayList<String>();
        cmds.addAll(this.subCommands.keySet());
        return cmds;
    }
}
