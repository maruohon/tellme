package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.ArrayList;
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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
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

        this.addSubCommandHelp("_generic", "Calculates the number of each block type in a given area");
        this.addSubCommandHelp("count", "Counts all the blocks in the given area");
        this.addSubCommandHelp("dump", "Dumps the stats from a previous 'count' command into a file in config/tellme/");
        this.addSubCommandHelp("query", "Prints the stats from a previous 'count' command into the console");
    }

    @Override
    public String getName()
    {
        return "blockstats";
    }

    private void printUsageCount(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " count all-loaded-chunks"));
        sender.sendMessage(new TextComponentString(pre + " count chunk-radius <radius>"));
        sender.sendMessage(new TextComponentString(pre + " count <x-distance> <y-distance> <z-distance>"));
        sender.sendMessage(new TextComponentString(pre + " count <xMin> <yMin> <zMin> <xMax> <yMax> <zMax>"));
    }

    private void printUsageDump(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " dump"));
        sender.sendMessage(new TextComponentString(pre + " dump [modid:blockname[:meta] modid:blockname[:meta] ...]"));
    }

    private void printUsageQuery(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
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
        else if (args.length == 2)
        {
            if (args[0].equals("dump") || args[0].equals("query"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, ForgeRegistries.BLOCKS.getKeys());
            }
            else if (args[0].equals("count"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "all-loaded-chunks", "chunk-radius");
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // "/tellme bockstats"
        if (args.length < 1)
        {
            this.sendMessage(sender, "Usage:");
            this.printUsageCount(sender);
            this.printUsageDump(sender);
            this.printUsageQuery(sender);
            return;
        }

        super.execute(server, sender, args);

        if ((sender instanceof EntityPlayer) == false)
        {
            throw new WrongUsageException("This command can only be run by a player");
        }

        String pre = this.getSubCommandUsagePre();
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
                    this.sendMessage(sender, "Calculating block statistics...");
                    int rx = Math.abs(CommandBase.parseInt(args[1]));
                    int ry = Math.abs(CommandBase.parseInt(args[2]));
                    int rz = Math.abs(CommandBase.parseInt(args[3]));
                    blockStats.calculateBlockStats(player.getEntityWorld(), player.getPosition(), rx, ry, rz);
                    this.sendMessage(sender, "Done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException("Usage: " + pre + " count <x-distance> <y-distance> <z-distance>");
                }
            }
            // cuboid corners
            else if (args.length == 7)
            {
                try
                {
                    BlockPos pos1 = CommandBase.parseBlockPos(player, args, 1, false);
                    BlockPos pos2 = CommandBase.parseBlockPos(player, args, 4, false);

                    this.sendMessage(sender, "Calculating block statistics...");
                    blockStats.calculateBlockStats(player.getEntityWorld(), pos1, pos2);
                    this.sendMessage(sender, "Done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException("Usage: " + pre + " count <x-min> <y-min> <z-min> <x-max> <y-max> <z-max>");
                }
            }
            else if (args.length == 2 && args[1].equals("all-loaded-chunks"))
            {
                this.sendMessage(sender, "Calculating block statistics...");
                blockStats.calculateBlockStatsForAllLoadedChunks(player.getEntityWorld());
                this.sendMessage(sender, "Done");
            }
            else if (args.length == 3 && args[1].equals("chunk-radius"))
            {
                try
                {
                    int r = Integer.parseInt(args[2]);
                    int count = (r * 2 + 1) * (r * 2 + 1);
                    this.sendMessage(sender, "Loading all the " + count + " chunks in the given radius of " + r + " chunks ...");
                    ChunkPos center = new ChunkPos(player.getPosition().getX() >> 4, player.getPosition().getZ() >> 4);
                    List<Chunk> chunks = new ArrayList<>();

                    for (int cZ = center.z - r; cZ <= center.z + r; cZ++)
                    {
                        for (int cX = center.x - r; cX <= center.x + r; cX++)
                        {
                            chunks.add(player.getEntityWorld().getChunkFromChunkCoords(cX, cZ));
                        }
                    }

                    this.sendMessage(sender, "Calculating block statistics for the selected " + chunks.size() + " chunks...");
                    blockStats.calculateBlockStatsForChunks(chunks);
                    this.sendMessage(sender, "Done");
                }
                catch (NumberFormatException e)
                {
                    throw new WrongUsageException("Usage: " + pre + " count chunk-radius <radius>");
                }
            }
            else
            {
                this.printUsageCount(sender);
                throw new CommandException("Invalid number of arguments!");
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
                this.sendMessage(sender, "Command output printed to console");
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
