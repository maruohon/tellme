package fi.dy.masa.tellme.command;

import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class SubCommandHelp extends SubCommand
{
    public SubCommandHelp(CommandTellme baseCommand)
    {
        super(baseCommand);
    }

    @Override
    public String getName()
    {
        return "help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            return;
        }

        if (args.length > 2)
        {
            throw new WrongUsageException("tellme.command.info.usage",
                "/" + this.getBaseCommand().getName() + " " + getName() + " [",
                "tellme.command.info.name",
                "]");
        }

        if (args.length == 2)
        {
            this.sendMessage(sender, "tellme.subcommand." + args[1] + ".info.main");
            return;
        }

        // args.length == 1, ie. "/tellme help"
        this.sendMessage(sender, "tellme.command.info.commands.available");
        List<String> subCommands = this.getBaseCommand().getSubCommandList();
        Collections.sort(subCommands);

        for (int i = 0; i < subCommands.size(); i++)
        {
            sender.sendMessage(new TextComponentString("/" + this.getBaseCommand().getName() + " " + subCommands.get(i)));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        // "/tellme help ???"
        if (args.length == 2)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getBaseCommand().getSubCommandList());
        }

        return Collections.<String>emptyList();
    }
}
