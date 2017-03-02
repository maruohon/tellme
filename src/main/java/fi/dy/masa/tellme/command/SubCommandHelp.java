package fi.dy.masa.tellme.command;

import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
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
        if (args.length > 1)
        {
            throw new CommandException("tellme.command.info.usage", this.getUsageCommon() + " <command>");
        }

        if (args.length == 1)
        {
            this.sendMessage(sender, "tellme.subcommand." + args[0] + ".info.main");
            return;
        }

        // args.length == 0, ie. "/tellme help"
        this.sendMessage(sender, "tellme.command.info.commands.available");
        List<String> subCommands = this.getBaseCommand().getSubCommandList();
        Collections.sort(subCommands);

        for (String name : subCommands)
        {
            sender.sendMessage(new TextComponentString("/" + this.getBaseCommand().getName() + " " + name));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        // "/tellme help subcommand"
        if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getBaseCommand().getSubCommandList());
        }

        return Collections.<String>emptyList();
    }
}
