package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public abstract class SubCommand implements ISubCommand
{
    protected final CommandTellme baseCommand;
    protected final ArrayList<String> subSubCommands = new ArrayList<String>();

    public SubCommand(CommandTellme baseCommand)
    {
        this.baseCommand = baseCommand;
        this.subSubCommands.add("help");
    }

    public CommandTellme getBaseCommand()
    {
        return this.baseCommand;
    }

    @Override
    public List<String> getSubCommands()
    {
        return this.subSubCommands;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 1 || (args.length == 2 && args[0].equals("help")))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getSubCommands());
        }

        return Collections.emptyList();
    }

    @Override
    public ITextComponent getHelp()
    {
        if (this.subSubCommands.size() == 0)
        {
            return new TextComponentString("");
        }

        return new TextComponentTranslation("tellme.subcommand.info.available", String.join(", ", this.subSubCommands));
    }

    protected String getUsageCommon()
    {
        return "/" + this.getBaseCommand().getName() + " " + this.getName();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // "/tellme sub-command"
        if (args.length == 0)
        {
            sender.sendMessage(this.getHelp());
        }
        // "/tellme sub-command [help|unknown]"
        else if (args.length == 1)
        {
            if (args[0].equals("help"))
            {
                sender.sendMessage(this.getHelp());
            }
            else if (this.subSubCommands.contains(args[0]) == false)
            {
                throw new WrongUsageException("tellme.subcommand.info.unknown", args[0]);
            }
        }
        // "/tellme sub-command help sub-sub-command"
        else if (args.length == 2 && args[0].equals("help"))
        {
            if (args[1].equals("help"))
            {
                this.sendMessage(sender, "tellme.subcommand.info.help");
            }
            else if (this.subSubCommands.contains(args[1]))
            {
                this.sendMessage(sender, "tellme.subcommand." + this.getName() + ".info." + args[1]);
            }
            else
            {
                throw new WrongUsageException("tellme.subcommand.info.unknown", args[1]);
            }
        }
    }

    protected void sendMessage(ICommandSender sender, String message, Object... args)
    {
        sender.sendMessage(new TextComponentTranslation(message, args));
    }

    public static String[] dropFirstStrings(String[] input, int toDrop)
    {
        if (toDrop >= input.length)
        {
            return new String[0];
        }

        String[] arr = new String[input.length - toDrop];
        System.arraycopy(input, toDrop, arr, 0, input.length - toDrop);
        return arr;
    }
}
