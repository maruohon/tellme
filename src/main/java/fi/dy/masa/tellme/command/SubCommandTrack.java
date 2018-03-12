package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.event.datalogging.DataLogger;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;
import fi.dy.masa.tellme.event.datalogging.EventManager;

public class SubCommandTrack extends SubCommand
{
    private final List<String> dataTypes = new ArrayList<>();

    public SubCommandTrack(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("add-log");
        this.subSubCommands.add("add-print");
        this.subSubCommands.add("clear-data");
        this.subSubCommands.add("enable");
        this.subSubCommands.add("disable");
        this.subSubCommands.add("dump");
        this.subSubCommands.add("print");
        this.subSubCommands.add("remove-log");
        this.subSubCommands.add("remove-print");

        this.dataTypes.add("chunk-load");
        this.dataTypes.add("chunk-unload");

        this.addSubCommandHelp("_generic", "Can track various events, such as chunk loads/unloads, entities joining the world, etc.");
    }

    @Override
    public String getName()
    {
        return "track";
    }

    private void printUsageTrack(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " <enable | disable> <all-dims | dimId> [all | type ... ]"));
        sender.sendMessage(new TextComponentString(pre + " <add-log | remove-log> <all-dims | dimId> [all | type ... ]"));
        sender.sendMessage(new TextComponentString(pre + " <add-print | remove-print> <all-dims | dimId> [all | type ... ]"));
        sender.sendMessage(new TextComponentString(pre + " clear-data <all-dims | dimId> [all | type ... ]"));
        sender.sendMessage(new TextComponentString(pre + " <dump | print> <all-dims | dimId> [all | type ... ]"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length < 1)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.subSubCommands);
        }
        else if (args.length == 2)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "all-dims");
        }
        else if (args.length >= 3)
        {
            List<String> strs = new ArrayList<>(this.dataTypes);
            strs.add("all");
            return CommandBase.getListOfStringsMatchingLastWord(args, strs);
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (this.isValidCommand(args, sender) == false)
        {
            this.printUsageTrack(sender);
            return;
        }

        String cmd = args[0];

        for (int i = 2; i < args.length; i++)
        {
            DataType type = DataType.fromArgument(args[i]);

            if (type == null)
            {
                continue;
            }

            if (cmd.equals("add-log"))
            {
                EventManager.registerHandler(type);
                DataLogger.instance().setLoggingEnabled(type, true);
                this.sendMessage(sender, "Enabled logging mode for %s", type.getOutputName());
            }
            else if (cmd.equals("remove-log"))
            {
                DataLogger.instance().setLoggingEnabled(type, false);
            }
            else if (cmd.equals("add-print"))
            {
                EventManager.registerHandler(type);
                DataLogger.instance().setPrintingEnabled(type, true);
                this.sendMessage(sender, "Enabled immediate-print mode for %s", type.getOutputName());
            }
            else if (cmd.equals("remove-print"))
            {
                // TODO unregister handler if no active prints/dumps left
                DataLogger.instance().setPrintingEnabled(type, false);
            }
            else if (cmd.equals("clear-data"))
            {
                DataLogger.instance().clearData(type);
            }
            else if (cmd.equals("dump"))
            {
                DataLogger.instance().dumpData(type, DataDump.Format.ASCII);
            }
        }
    }

    private boolean isValidCommand(String[] args, ICommandSender sender)
    {
        if (args.length < 3)
        {
            this.sendMessage(sender, "Too few arguments");
            return false;
        }

        if (this.subSubCommands.contains(args[0]) == false)
        {
            this.sendMessage(sender, "Invalid sub-command '%s'", args[1]);
            return false;
        }

        if (args[1].equals("all-dims") == false)
        {
            try
            {
                Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e)
            {
                this.sendMessage(sender, "Invalid dimension id '%s'", args[1]);
                return false;
            }
        }

        for (int i = 2; i < args.length; i++)
        {
            if (this.dataTypes.contains(args[i]) == false)
            {
                this.sendMessage(sender, "Invalid data type '%s'", args[i]);
                return false;
            }
        }

        return true;
    }
}
