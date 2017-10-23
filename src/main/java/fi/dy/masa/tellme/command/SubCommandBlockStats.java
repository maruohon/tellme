package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.google.common.collect.Maps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.util.BlockStats;

public class SubCommandBlockStats extends SubCommand
{
    private final Map<UUID, BlockStats> blockStats = Maps.newHashMap();

    public SubCommandBlockStats(CommandTellme baseCommand)
    {
        super(baseCommand);
        this.subSubCommands.add("count");
        this.subSubCommands.add("dump");
        this.subSubCommands.add("query");
    }

    @Override
    public String getName()
    {
        return "blockstats";
    }

    private void printUsageCount(ICommandSender sender)
    {
        String pre = this.getUsageCommon();
        sender.sendMessage(new TextComponentString(pre + " count <x-distance> <y-distance> <z-distance>"));
        sender.sendMessage(new TextComponentString(pre + " count <xMin> <yMin> <zMin> <xMax> <yMax> <zMax>"));
    }

    private void printUsageDump(ICommandSender sender)
    {
        String pre = this.getUsageCommon();
        sender.sendMessage(new TextComponentString(pre + " dump"));
        sender.sendMessage(new TextComponentString(pre + " dump [modid:blockname[:meta] modid:blockname[:meta] ...]"));
    }

    private void printUsageQuery(ICommandSender sender)
    {
        String pre = this.getUsageCommon();
        sender.sendMessage(new TextComponentString(pre + " query"));
        sender.sendMessage(new TextComponentString(pre + " query [modid:blockname[:meta] modid:blockname[:meta] ...]"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "count", "dump", "query");
        }
        else if (args.length == 2 && (args[0].equals("dump") || args[0].equals("query")))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, ForgeRegistries.BLOCKS.getKeys());
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // "/tellme bockstats"
        if (args.length < 1)
        {
            this.sendMessage(sender, "tellme.command.info.usage.noparam");
            this.printUsageCount(sender);
            this.printUsageDump(sender);
            this.printUsageQuery(sender);
            return;
        }

        super.execute(server, sender, args);

        if ((sender instanceof EntityPlayer) == false)
        {
            throw new WrongUsageException("tellme.subcommand.blockstats.error.notplayer");
        }

        String pre = this.getUsageCommon();
        EntityPlayer player = (EntityPlayer) sender;
        BlockStats blockStats = this.getBlockStatsForPlayer(player);

        // Possible command formats are:
        // /tellme blockstats count <x-distance> <y-distance> <z-distance>
        // /tellme blockstats count <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>
        // /tellme blockstats query
        // /tellme blockstats query [blockname blockname ...]

        // "/tellme blockstats count ..."
        if (args[0].equals("count"))
        {
            // range
            if (args.length == 4)
            {
                try
                {
                    this.sendMessage(sender, "tellme.subcommand.blockstats.calculating");
                    int rx = Math.abs(CommandBase.parseInt(args[1]));
                    int ry = Math.abs(CommandBase.parseInt(args[2]));
                    int rz = Math.abs(CommandBase.parseInt(args[3]));
                    blockStats.calculateBlockStats(player.getEntityWorld(), player.getPosition(), rx, ry, rz);
                    this.sendMessage(sender, "tellme.command.info.done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException("tellme.command.info.usage", pre + " count <x-distance> <y-distance> <z-distance>");
                }
            }
            // cuboid corners
            else if (args.length == 7)
            {
                try
                {
                    BlockPos pos1 = CommandBase.parseBlockPos(player, args, 1, false);
                    BlockPos pos2 = CommandBase.parseBlockPos(player, args, 4, false);

                    this.sendMessage(sender, "tellme.subcommand.blockstats.calculating");
                    blockStats.calculateBlockStats(player.getEntityWorld(), pos1, pos2);
                    this.sendMessage(sender, "tellme.command.info.done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException("tellme.command.info.usage", pre + " count <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>");
                }
            }
            else
            {
                this.printUsageCount(sender);
                this.printUsageDump(sender);
                this.printUsageQuery(sender);
                throw new CommandException("tellme.command.error.argument.invalid.number");
            }
        }
        // "/tellme blockstats query ..." or "/tellme blockstats dump ..."
        else if (args[0].equals("query") || args[0].equals("dump"))
        {
            List<String> lines;

            // We have some filters specified
            if (args.length > 1)
            {
                lines = blockStats.query(Arrays.asList(dropFirstStrings(args, 1)));
            }
            else
            {
                lines = blockStats.queryAll();
            }

            if (args[0].equals("query"))
            {
                DataDump.printDataToLogger(lines);
                this.sendMessage(sender, "tellme.info.output.to.console");
            }
            else // dump
            {
                File file = DataDump.dumpDataToFile("block_stats", lines);
                sendClickableLinkMessage(player, "Output written to file %s", file);
            }
        }
    }

    private BlockStats getBlockStatsForPlayer(EntityPlayer player)
    {
        BlockStats stats = this.blockStats.get(player.getUniqueID());

        if (stats == null)
        {
            stats = new BlockStats();
            this.blockStats.put(player.getUniqueID(), stats);
        }

        return stats;
    }
}
