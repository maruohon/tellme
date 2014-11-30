package fi.dy.masa.tellme.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<String> addTabCompletionOptions(ICommandSender icommandsender, String[] args)
    {
        if (args.length == 3 && args[1].equals("count"))
        {
            MinecraftServer srv = MinecraftServer.getServer();
            if (srv != null)
            {
                return CommandBase.getListOfStringsFromIterableMatchingLastWord(args, Arrays.asList(srv.getConfigurationManager().getAllUsernames()));
            }
        }

        return super.addTabCompletionOptions(icommandsender, args);
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] args)
    {
        // "/tellme bockstats"
        if (args.length < 2)
        {
            String pre = "/" + CommandTellme.instance.getCommandName() + " " + this.getCommandName();

            icommandsender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.command.usage") + ": "));
            icommandsender.addChatMessage(new ChatComponentText(pre + " count <playername> <x-distance> <y-distance> <z-distance>"));
            icommandsender.addChatMessage(new ChatComponentText(pre + " count <dimension> <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>"));
            icommandsender.addChatMessage(new ChatComponentText(pre + " query"));
            icommandsender.addChatMessage(new ChatComponentText(pre + " query [blockname blockname ...]"));
            icommandsender.addChatMessage(new ChatComponentText(pre + " dump"));
            icommandsender.addChatMessage(new ChatComponentText(pre + " dump [blockname blockname ...]"));

            return;
        }

        super.processCommand(icommandsender, args);

        if (icommandsender instanceof EntityPlayer == false)
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
                    EntityPlayer player = srv.getConfigurationManager().func_152612_a(args[2]);
                    if (player != null)
                    {
                        icommandsender.addChatMessage(new ChatComponentTranslation("info.subcommand.blockstats.calculating"));
                        this.blockStats.calculateBlockStats(player, Arrays.asList(args).subList(3, args.length));
                        icommandsender.addChatMessage(new ChatComponentTranslation("info.command.done"));
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
                icommandsender.addChatMessage(new ChatComponentTranslation("info.subcommand.blockstats.calculating"));
                this.blockStats.calculateBlockStats(Arrays.asList(args).subList(2, args.length));
                icommandsender.addChatMessage(new ChatComponentTranslation("info.command.done"));
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
                icommandsender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.output.to.console")));
            }
            else // dump
            {
                DataDump.dumpDataToFile("block_stats", this.blockStats.getBlockStatsLines());
                icommandsender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.output.to.file.cfgdir")));
            }
        }
    }
}
