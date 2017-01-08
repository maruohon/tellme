package fi.dy.masa.tellme.command;

import java.util.ArrayList;
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
        if (args.length == 2 || (args.length == 3 && args[1].equals("help")))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getSubCommands());
        }

        return null;
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

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // "/tellme sub-command"
        if (args.length == 1)
        {
            sender.sendMessage(this.getHelp());
        }
        // "/tellme sub-command [help|unknown]"
        else if (args.length == 2)
        {
            if (args[1].equals("help"))
            {
                sender.sendMessage(this.getHelp());
            }
            else if (this.subSubCommands.contains(args[1]) == false)
            {
                throw new WrongUsageException("tellme.subcommand.info.unknown", args[1]);
            }
        }
        // "/tellme sub-command help sub-sub-command"
        else if (args.length == 3 && args[1].equals("help"))
        {
            if (args[2].equals("help"))
            {
                this.sendMessage(sender, "tellme.subcommand.info.help");
            }
            else if (this.subSubCommands.contains(args[2]))
            {
                this.sendMessage(sender, "tellme.subcommand." + args[0] + ".info." + args[2]);
            }
            else
            {
                throw new WrongUsageException("tellme.subcommand.info.unknown", args[3]);
            }
        }
    }

    protected void sendMessage(ICommandSender sender, String message, Object... args)
    {
        sender.sendMessage(new TextComponentTranslation(message, args));
    }
}
