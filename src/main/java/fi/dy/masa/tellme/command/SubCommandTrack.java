package fi.dy.masa.tellme.command;

import java.io.File;
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

public class SubCommandTrack extends SubCommand
{
    private final List<String> dataTypes = new ArrayList<>();

    public SubCommandTrack(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("add-filter");
        this.subSubCommands.add("add-log");
        this.subSubCommands.add("add-print");
        this.subSubCommands.add("clear-data");
        this.subSubCommands.add("enable");
        this.subSubCommands.add("enable-filters");
        this.subSubCommands.add("disable");
        this.subSubCommands.add("disable-filters");
        this.subSubCommands.add("dump");
        this.subSubCommands.add("print");
        this.subSubCommands.add("remove-filter");
        this.subSubCommands.add("remove-log");
        this.subSubCommands.add("remove-print");
        this.subSubCommands.add("show-loggers");

        this.dataTypes.add("all");
        this.dataTypes.add("chunk-load");
        this.dataTypes.add("chunk-unload");
        this.dataTypes.add("entity-join-world");

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
        sender.sendMessage(new TextComponentString(pre + " <enable-filters | disable-filters> <all-dims | dimId> [all | type ... ]"));
        sender.sendMessage(new TextComponentString(pre + " <add-filter | remove-filter> <all-dims | dimId> <type> filters ..."));
        sender.sendMessage(new TextComponentString(pre + " show-loggers <all-dims | dimId>"));
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
            return CommandBase.getListOfStringsMatchingLastWord(args, this.dataTypes);
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
        int[] dims;

        if (args[1].equals("all-dims"))
        {
            dims = new int[] { 0, -1, 1 };
        }
        else
        {
            dims = new int[] { CommandBase.parseInt(args[1]) };
        }

        DataDump dumpLoggers = new DataDump(5, DataDump.Format.ASCII);

        for (int dimension : dims)
        {
            if (cmd.equals("show-loggers"))
            {
                DataLogger.instance(dimension).printLoggers(dumpLoggers);
                continue;
            }

            DataType[] types;

            if (args[2].equals("all"))
            {
                types = DataType.values();
            }
            else
            {
                types = new DataType[args.length - 2];

                for (int i = 2, j = 0; i < args.length; i++, j++)
                {
                    types[j] = DataType.fromArgument(args[i]);
                }
            }

            for (DataType type : types)
            {
                if (type == null)
                {
                    continue;
                }

                if (cmd.equals("add-log") || cmd.equals("remove-log"))
                {
                    boolean enable = cmd.equals("add-log");
                    String msg = enable ? "Enabled" : "Disabled";

                    if (DataLogger.instance(dimension).setLoggingEnabled(type, enable))
                    {
                        this.sendMessage(sender, msg + " logging mode for '%s' in dimension %s", type.getOutputName(), dimension);
                    }
                }
                else if (cmd.equals("add-print") || cmd.equals("remove-print"))
                {
                    boolean enable = cmd.equals("add-print");
                    String msg = enable ? "Enabled" : "Disabled";

                    if (DataLogger.instance(dimension).setPrintingEnabled(type, enable))
                    {
                        this.sendMessage(sender, msg + " immediate-print mode for '%s' in dimension %s", type.getOutputName(), dimension);
                    }
                }
                else if (cmd.equals("enable") || cmd.equals("disable"))
                {
                    boolean enable = cmd.equals("enable");
                    String msg = enable ? "Enabled" : "Disabled";

                    if (DataLogger.instance(dimension).setEnabled(type, enable))
                    {
                        this.sendMessage(sender, msg + " tracking of '%s' in dimension %s", type.getOutputName(), dimension);
                    }
                }
                else if (args.length >= 4 && (cmd.equals("add-filter") || cmd.equals("remove-filter")))
                {
                    boolean add = cmd.equals("add-filter");
                    String msg = add ? "Added" : "Removed";
                    DataLogger.instance(dimension).modifyFilters(type, add, dropFirstStrings(args, 3));
                    this.sendMessage(sender, msg + " filters for '%s' in dimension %s", type.getOutputName(), dimension);
                }
                else if (cmd.equals("enable-filters") || cmd.equals("disable-filters"))
                {
                    boolean enable = cmd.equals("enable-filters");
                    String msg = enable ? "Enabled" : "Disabled";

                    if (DataLogger.instance(dimension).setFilterEnabled(type, enable))
                    {
                        this.sendMessage(sender, msg + " filters for '%s' in dimension %s", type.getOutputName(), dimension);
                    }
                }
                else if (cmd.equals("clear-data"))
                {
                    DataLogger.instance(dimension).clearData(type);
                    this.sendMessage(sender, "Cleared logged data for '%s' in dimension %s", type.getOutputName(), dimension);
                }
                else if (cmd.equals("dump"))
                {
                    File file = DataLogger.instance(dimension).dumpData(type, DataDump.Format.ASCII);

                    if (file != null)
                    {
                        String str = String.format("Dumped logged data for '%s' in dimension %d to file %%s", type.getOutputName(), dimension);
                        sendClickableLinkMessage(sender, str, file);
                    }
                }
            }
        }

        if (cmd.equals("show-loggers"))
        {
            dumpLoggers.addFooter("Currently enabled loggers/printers");
            dumpLoggers.addTitle("Dim", "Type", "Enabled", "Print", "Log");

            dumpLoggers.setColumnProperties(0, DataDump.Alignment.RIGHT, true);
            dumpLoggers.setColumnAlignment(2, DataDump.Alignment.RIGHT);
            dumpLoggers.setColumnAlignment(3, DataDump.Alignment.RIGHT);
            dumpLoggers.setColumnAlignment(4, DataDump.Alignment.RIGHT);
            dumpLoggers.setUseColumnSeparator(true);

            DataDump.printDataToLogger(dumpLoggers.getLines());
            this.sendMessage(sender, "Output printed to console");
        }
    }

    private boolean isValidCommand(String[] args, ICommandSender sender)
    {
        if (args.length < 2 || (args.length < 3 && args[0].equals("show-loggers") == false))
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

        if (args.length >= 3)
        {
            if (args[0].equals("add-filter") || args[0].equals("remove-filter"))
            {
                if (args.length < 4)
                {
                    return false;
                }
                else
                {
                    return true;
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
        }

        return true;
    }
}
