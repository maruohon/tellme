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
        registerSubCommand(new SubCommandDump());
        registerSubCommand(new SubCommandHelp());
    }

    @Override
    public String getCommandName()
    {
        return "tellme";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + this.getCommandName() + " help";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] strArr, BlockPos pos)
    {
        if (strArr.length == 1)
        {
            return getListOfStringsMatchingLastWord(strArr, subCommands.keySet());
        }
        else if (subCommands.containsKey(strArr[0]))
        {
            ISubCommand sc = subCommands.get(strArr[0]);
            if (sc != null)
            {
                return sc.getTabCompletionOptions(server, sender, strArr);
            }
        }
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] commandArgs) throws CommandException
    {
        if (commandArgs.length > 0)
        {
            if (subCommands.containsKey(commandArgs[0]) == true)
            {
                ISubCommand cb = subCommands.get(commandArgs[0]);
                if (cb != null)
                {
                    cb.execute(server, sender, commandArgs);
                    return;
                }
            }
            else
            {
                throw new WrongUsageException(I18n.translateToLocal("info.command.unknown") + ": /" + this.getCommandName() + " " + commandArgs[0], new Object[0]);
            }
        }

        throw new WrongUsageException(I18n.translateToLocal("info.command.help") + ": '" + getCommandUsage(sender) + "'", new Object[0]);
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
