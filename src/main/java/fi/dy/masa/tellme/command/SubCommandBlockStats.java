package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Arrays;
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

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if ((sender instanceof EntityPlayer) == false)
        {
            throw new WrongUsageException("tellme.subcommand.blockstats.error.notplayer");
        }

        EntityPlayer player = (EntityPlayer) sender;
        String pre = "/" + this.getBaseCommand().getName() + " " + this.getName();

        // "/tellme bockstats"
        if (args.length < 2)
        {
            this.sendMessage(sender, "tellme.command.info.usage.noparam");
            player.sendMessage(new TextComponentString(pre + " count <x-distance> <y-distance> <z-distance>"));
            player.sendMessage(new TextComponentString(pre + " count <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>"));
            player.sendMessage(new TextComponentString(pre + " query"));
            player.sendMessage(new TextComponentString(pre + " query [modid:blockname[:meta] modid:blockname[:meta] ...]"));
            player.sendMessage(new TextComponentString(pre + " dump"));
            player.sendMessage(new TextComponentString(pre + " dump [modid:blockname[:meta] modid:blockname[:meta] ...]"));

            return;
        }

        super.execute(server, sender, args);

        BlockStats blockStats = this.getBlockStats(player);

        // Possible command formats are:
        // /tellme blockstats count <x-distance> <y-distance> <z-distance>
        // /tellme blockstats count <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>
        // /tellme blockstats query
        // /tellme blockstats query [blockname blockname ...]

        // "/tellme blockstats count ..."
        if (args[1].equals("count"))
        {
            // range
            if (args.length == 5)
            {
                try
                {
                    this.sendMessage(sender, "tellme.subcommand.blockstats.calculating");
                    int rx = Math.abs(CommandBase.parseInt(args[2]));
                    int ry = Math.abs(CommandBase.parseInt(args[3]));
                    int rz = Math.abs(CommandBase.parseInt(args[4]));
                    blockStats.calculateBlockStats(player.getEntityWorld(), player.getPosition(), rx, ry, rz);
                    this.sendMessage(sender, "tellme.command.info.done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException("tellme.command.info.usage", pre + " count <x-distance> <y-distance> <z-distance>");
                }
            }
            // cuboid corners
            else if (args.length == 8)
            {
                try
                {
                    this.sendMessage(sender, "tellme.subcommand.blockstats.calculating");
                    BlockPos pos1 = CommandBase.parseBlockPos(player, args, 2, false);
                    BlockPos pos2 = CommandBase.parseBlockPos(player, args, 5, false);
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
                throw new WrongUsageException("tellme.command.error.argument.invalid.number");
            }
        }
        // "/tellme blockstats query ..." or "/tellme blockstats dump ..."
        else if (args[1].equals("query") || args[1].equals("dump"))
        {
            // We have some filters specified
            if (args.length > 2)
            {
                blockStats.query(Arrays.asList(args).subList(2, args.length));
            }
            else
            {
                blockStats.queryAll();
            }

            if (args[1].equals("query"))
            {
                blockStats.printBlockStatsToLogger();
                this.sendMessage(sender, "tellme.info.output.to.console");
            }
            else // dump
            {
                File f = DataDump.dumpDataToFile("block_stats", blockStats.getBlockStatsLines());
                this.sendMessage(sender, "tellme.info.output.to.file", f.getName());
            }
        }
    }

    private BlockStats getBlockStats(EntityPlayer player)
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
