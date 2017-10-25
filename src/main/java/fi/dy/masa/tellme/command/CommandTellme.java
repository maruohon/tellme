package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
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
        this.registerSubCommand(new SubCommandDumpCsv(this));
        this.registerSubCommand(new SubCommandHelp(this));
        this.registerSubCommand(new SubCommandHolding(this));
        this.registerSubCommand(new SubCommandList(this));
        this.registerSubCommand(new SubCommandListCsv(this));
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
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, this.subCommands.keySet());
        }
        else if (this.subCommands.containsKey(args[0]))
        {
            ISubCommand sc = this.subCommands.get(args[0]);

            if (sc != null)
            {
                return sc.getTabCompletions(server, sender, SubCommand.dropFirstStrings(args, 1));
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            args = new String[] { "help" };
        }

        if (this.subCommands.containsKey(args[0]))
        {
            ISubCommand cmd = this.subCommands.get(args[0]);

            if (cmd != null)
            {
                cmd.execute(server, sender, SubCommand.dropFirstStrings(args, 1));
                return;
            }
        }
        else
        {
            throw new CommandException("Unrecognized command: /" + this.getName() + " " + args[0]);
        }
    }

    private void registerSubCommand(ISubCommand cmd)
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
