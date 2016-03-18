package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;

import fi.dy.masa.tellme.util.BlockStats;
import fi.dy.masa.tellme.util.DataDump;

public class SubCommandBlockStats extends SubCommand
{
    private BlockStats blockStats;

    public SubCommandBlockStats()
    {
        super();
        this.blockStats = new BlockStats();
        this.subSubCommands.add("count");
        this.subSubCommands.add("dump");
        this.subSubCommands.add("query");
    }

    @Override
    public String getCommandName()
    {
        return "blockstats";
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 3 && args[1].equals("count"))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList(server.getAllUsernames()));
        }

        return super.getTabCompletionOptions(server, sender, args);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // "/tellme bockstats"
        if (args.length < 2)
        {
            String pre = "/" + CommandTellme.instance.getCommandName() + " " + this.getCommandName();

            sender.addChatMessage(new TextComponentString(I18n.translateToLocal("info.command.usage") + ": "));
            sender.addChatMessage(new TextComponentString(pre + " count <playername> <x-distance> <y-distance> <z-distance>"));
            sender.addChatMessage(new TextComponentString(pre + " count <dimension> <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>"));
            sender.addChatMessage(new TextComponentString(pre + " query"));
            sender.addChatMessage(new TextComponentString(pre + " query [modid:blockname[:meta] modid:blockname[:meta] ...]"));
            sender.addChatMessage(new TextComponentString(pre + " dump"));
            sender.addChatMessage(new TextComponentString(pre + " dump [modid:blockname[:meta] modid:blockname[:meta] ...]"));

            return;
        }

        super.execute(server, sender, args);

        if (sender instanceof EntityPlayer == false)
        {
            throw new WrongUsageException(I18n.translateToLocal("info.subcommand.blockstats.notplayer"));
        }

        // Possible command formats are:
        // /tellme blockstats count <playername> <x-distance> <y-distance> <z-distance>
        // /tellme blockstats count <dimension> <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>
        // /tellme blockstats query
        // /tellme blockstats query [blockname blockname ...]

        // "/tellme blockstats count ..."
        if (args[1].equals("count"))
        {
            // player, range
            if (args.length == 6)
            {
                // Get the player entity matching the name given as parameter
                EntityPlayer player = server.getPlayerList().getPlayerByUsername(args[2]);
                if (player != null)
                {
                    sender.addChatMessage(new TextComponentString(I18n.translateToLocal("info.subcommand.blockstats.calculating")));
                    this.blockStats.calculateBlockStats(player, Arrays.asList(args).subList(3, args.length));
                    sender.addChatMessage(new TextComponentString(I18n.translateToLocal("info.command.done")));
                }
                else
                {
                    throw new WrongUsageException(I18n.translateToLocal("info.command.player.notfound") + ": '" + args[2] + "'");
                }
            }
            // cuboid corners
            else if (args.length == 9)
            {
                sender.addChatMessage(new TextComponentString(I18n.translateToLocal("info.subcommand.blockstats.calculating")));
                this.blockStats.calculateBlockStats(Arrays.asList(args).subList(2, args.length));
                sender.addChatMessage(new TextComponentString(I18n.translateToLocal("info.command.done")));
            }
            else
            {
                throw new WrongUsageException(I18n.translateToLocal("info.command.invalid.argument.number")
                    + " " + I18n.translateToLocal("info.command.usage") + ": /"
                    + CommandTellme.instance.getCommandName() + " " + this.getCommandName() + " count <playername> <x-distance> <y-distance> <z-distance>"
                    + " or /" + CommandTellme.instance.getCommandName() + " " + this.getCommandName()
                    + " count <dimension> <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>");
            }
        }
        // "/tellme blockstats query ..." or "/tellme blockstats dump ..."
        else if (args[1].equals("query") || args[1].equals("dump"))
        {
            // We have some filters specified
            if (args.length > 2)
            {
                this.blockStats.query(Arrays.asList(args).subList(2, args.length));
            }
            else
            {
                this.blockStats.queryAll();
            }

            if (args[1].equals("query"))
            {
                this.blockStats.printBlockStatsToLogger();
                sender.addChatMessage(new TextComponentString(I18n.translateToLocal("info.output.to.console")));
            }
            else // dump
            {
                File f = DataDump.dumpDataToFile("block_stats", this.blockStats.getBlockStatsLines());
                sender.addChatMessage(new TextComponentString("Output written to file " + f.getName()));
            }
        }
    }
}
