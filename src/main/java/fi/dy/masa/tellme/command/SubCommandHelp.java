package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

public class SubCommandHelp extends SubCommand
{
    public SubCommandHelp(CommandTellme baseCommand)
    {
        super(baseCommand);
    }

    @Override
    public String getCommandName()
    {
        return "help";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            return;
        }

        if (args.length > 2)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.usage") + ": /"
                + this.getBaseCommand().getCommandName() + " " + getCommandName() + " [" + StatCollector.translateToLocal("info.command.name") + "]");
        }

        if (args.length == 2)
        {
            sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.subcommand." + args[1])));
            return;
        }

        // args.length == 1, ie. "/tellme help"
        StringBuilder str = new StringBuilder(StatCollector.translateToLocal("info.command.available") + ": ");
        List<String> subCommands = new ArrayList<String>(this.getBaseCommand().getSubCommandList());

        for (int i = 0; i < subCommands.size() - 2; ++i)
        {
            str.append("/" + this.getBaseCommand().getCommandName() + " " + subCommands.get(i) + ", ");
        }

        if (subCommands.size() > 1)
        {
            str.append("/" + this.getBaseCommand().getCommandName() + " " + subCommands.get(subCommands.size() - 2) + " " + StatCollector.translateToLocal("info.and") + " ");
        }

        // Last or only command
        if (subCommands.size() >= 1)
        {
            str.append("/" + this.getBaseCommand().getCommandName() + " " + subCommands.get(subCommands.size() - 1));
        }

        // List of sub commands
        sender.addChatMessage(new ChatComponentText(str.toString()));

        // Sub command help
        sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.command.help.subcommand") + " '/" + this.getBaseCommand().getCommandName() + " <sub_command> help'"));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        // "/tellme help ???"
        if (args.length == 2)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getBaseCommand().getSubCommandList());
        }

        return null;
    }
}
