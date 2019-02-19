package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import fi.dy.masa.tellme.TellMe;

public class CommandDump
{
    public static List<String> getFormattedCommandDump(DataDump.Format format, ICommandSender sender)
    {
        DataDump dump = new DataDump(4, format);

        Map<String, ICommand> map = TellMe.proxy.getCommandHandler().getCommands();
        List<String> aliases = new ArrayList<>();

        for (Map.Entry<String, ICommand> entry : map.entrySet())
        {
            ICommand cmd = entry.getValue();
            String cmdName = cmd.getName();
            String className = cmd.getClass().getName();
            @SuppressWarnings("deprecation")
            String usage = net.minecraft.util.text.translation.I18n.translateToLocal(cmd.getUsage(sender));

            aliases.clear();
            aliases.addAll(cmd.getAliases());

            if (aliases.isEmpty() == false)
            {
                Collections.sort(aliases);
                dump.addData(className, cmdName, String.join(", ", aliases), usage);
            }
            else
            {
                dump.addData(className, cmdName, "", usage);
            }
        }

        dump.addTitle("Class", "Command", "Aliases", "Usage");
        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }
}
