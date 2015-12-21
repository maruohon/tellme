package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import fi.dy.masa.tellme.util.BlockStats;
import fi.dy.masa.tellme.util.DataDump;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

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
    public List<String> addTabCompletionOptions(ICommandSender icommandsender, String[] args)
    {
        if (args.length == 3 && args[1].equals("count"))
        {
            MinecraftServer srv = MinecraftServer.getServer();
            if (srv != null)
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList(srv.getConfigurationManager().getAllUsernames()));
            }
        }

        return super.addTabCompletionOptions(icommandsender, args);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        // "/tellme bockstats"
        if (args.length < 2)
        {
            String pre = "/" + CommandTellme.instance.getCommandName() + " " + this.getCommandName();

            sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.command.usage") + ": "));
            sender.addChatMessage(new ChatComponentText(pre + " count <playername> <x-distance> <y-distance> <z-distance>"));
            sender.addChatMessage(new ChatComponentText(pre + " count <dimension> <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>"));
            sender.addChatMessage(new ChatComponentText(pre + " query"));
            sender.addChatMessage(new ChatComponentText(pre + " query [modid:blockname[:meta] modid:blockname[:meta] ...]"));
            sender.addChatMessage(new ChatComponentText(pre + " dump"));
            sender.addChatMessage(new ChatComponentText(pre + " dump [modid:blockname[:meta] modid:blockname[:meta] ...]"));

            return;
        }

        super.processCommand(sender, args);

        if (sender instanceof EntityPlayer == false)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.subcommand.blockstats.notplayer"));
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
                MinecraftServer srv = MinecraftServer.getServer();
                if (srv != null)
                {
                    // Get the player entity matching the name given as parameter
                    EntityPlayer player = srv.getConfigurationManager().getPlayerByUsername(args[2]);
                    if (player != null)
                    {
                        sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.subcommand.blockstats.calculating")));
                        this.blockStats.calculateBlockStats(player, Arrays.asList(args).subList(3, args.length));
                        sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.command.done")));
                    }
                    else
                    {
                        throw new WrongUsageException(StatCollector.translateToLocal("info.command.player.notfound") + ": '" + args[2] + "'");
                    }
                }
            }
            // cuboid corners
            else if (args.length == 9)
            {
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.subcommand.blockstats.calculating")));
                this.blockStats.calculateBlockStats(Arrays.asList(args).subList(2, args.length));
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.command.done")));
            }
            else
            {
                throw new WrongUsageException(StatCollector.translateToLocal("info.command.invalid.argument.number")
                    + " " + StatCollector.translateToLocal("info.command.usage") + ": /"
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
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.output.to.console")));
            }
            else // dump
            {
                File f = DataDump.dumpDataToFile("block_stats", this.blockStats.getBlockStatsLines());
                sender.addChatMessage(new ChatComponentText("Output written to file " + f.getName()));
            }
        }
    }
}
