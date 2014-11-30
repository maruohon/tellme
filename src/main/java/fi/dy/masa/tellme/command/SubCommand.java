package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public abstract class SubCommand implements ISubCommand
{
    protected ArrayList<String> subSubCommands = new ArrayList<String>();

    public SubCommand()
    {
        this.subSubCommands.add("help");
    }

    @Override
    public List<String> getSubCommands()
    {
        return this.subSubCommands;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> addTabCompletionOptions(ICommandSender icommandsender, String[] args)
    {
        if (args.length == 2 || (args.length == 3 && args[1].equals("help")))
        {
            return CommandBase.getListOfStringsFromIterableMatchingLastWord(args, this.getSubCommands());
        }

        return null;
    }

    @Override
    public String getSubCommandsHelpString()
    {
        StringBuilder str = new StringBuilder(StatCollector.translateToLocal("info.subcommands.available") + ": ");

        for (int i = 0; i < this.subSubCommands.size() - 1; ++i)
        {
            str.append(this.subSubCommands.get(i) + ", ");
        }
        if (this.subSubCommands.size() >= 1)
        {
            str.append(this.subSubCommands.get(this.subSubCommands.size() - 1));
            return str.toString();
        }

        return "";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        // "/tellme command"
        if (args.length == 1)
        {
            sender.addChatMessage(new ChatComponentText(this.getSubCommandsHelpString()));
        }
        // "/tellme command [help|unknown]"
        else if (args.length == 2)
        {
            if (args[1].equals("help"))
            {
                sender.addChatMessage(new ChatComponentText(this.getSubCommandsHelpString()));
            }
            else if (this.subSubCommands.contains(args[1]) == false)
            {
                throw new WrongUsageException(StatCollector.translateToLocal("info.command.unknown.subcommand") + " '" + args[1] + "'");
            }
        }
        // "/tellme command help subsubcommand"
        else if (args.length == 3 && args[1].equals("help"))
        {
            if (args[2].equals("help"))
            {
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.subcommands.help")));
            }
            else if (this.subSubCommands.contains(args[2]) == true)
            {
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.subcommand." + args[0] + ".help." + args[2])));
            }
            else
            {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + StatCollector.translateToLocal("info.subcommands.help.unknown") + " " + args[3] + EnumChatFormatting.RESET));
            }
        }
    }
}
