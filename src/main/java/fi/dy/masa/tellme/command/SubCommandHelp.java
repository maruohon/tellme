package fi.dy.masa.tellme.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import malilib.util.position.BlockPos;

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
            throw new CommandException("Usage: " + this.getSubCommandUsagePre() + " <sub-command>");
        }

        if (args.length == 1)
        {
            System.out.printf("plop\n");
            String str = this.getSubCommandHelp("_generic");

            if (StringUtils.isBlank(str) == false)
            {
                this.sendMessage(sender, str);
            }
            else
            {
                this.sendMessage(sender, "No help available");
            }

            return;
        }

        // args.length == 0, ie. "/tellme help"
        this.sendMessage(sender, "Available commands are:");
        List<String> subCommands = this.getBaseCommand().getSubCommandList();
        Collections.sort(subCommands);

        for (String name : subCommands)
        {
            sender.sendMessage(new TextComponentString("/" + this.getBaseCommand().getName() + " " + name));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        // "/tellme help subcommand"
        if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getBaseCommand().getSubCommandList());
        }

        return Collections.<String>emptyList();
    }
}
