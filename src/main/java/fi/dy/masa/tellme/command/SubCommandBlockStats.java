package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.chunkprocessor.BlockStats;

public class SubCommandBlockStats extends SubCommand
{
    private final Map<UUID, BlockStats> blockStats = Maps.newHashMap();
    private final BlockStats blockStatsConsole = new BlockStats();

    public SubCommandBlockStats(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("count");
        this.subSubCommands.add("count-append");
        this.subSubCommands.add("dump");
        this.subSubCommands.add("dump-csv");
        this.subSubCommands.add("query");

        this.addSubCommandHelp("_generic", "Calculates the number of each block type in a given area");
        this.addSubCommandHelp("count", "Counts all the blocks in the given area");
        this.addSubCommandHelp("dump", "Dumps the stats from a previous 'count' command into a file in config/tellme/");
        this.addSubCommandHelp("dump-csv", "Dumps the stats from a previous 'count' command into a CSV file in config/tellme/");
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
        sender.sendMessage(new TextComponentString(pre + " count all-loaded-chunks [dimension]"));
        sender.sendMessage(new TextComponentString(pre + " count chunk-radius <radius> [dimension] [x y z (of the center)]"));
        sender.sendMessage(new TextComponentString(pre + " count range <x-distance> <y-distance> <z-distance> [dimension] [x y z (of the center)]"));
        sender.sendMessage(new TextComponentString(pre + " count box <x1> <y1> <z1> <x2> <y2> <z2> [dimension]"));
    }

    private void printUsageDump(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " dump[-csv] [sort-by-count] [modid:blockname[:meta] modid:blockname[:meta] ...]"));
    }

    private void printUsageQuery(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " query [sort-by-count] [modid:blockname[:meta] modid:blockname[:meta] ...]"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "count", "count-append", "dump", "dump-csv", "query");
        }
        else if (args.length >= 2)
        {
            if (args.length == 2 && args[0].equals("count"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "all-loaded-chunks", "box", "chunk-radius", "range");
            }
            else if (args[0].equals("dump") || args[0].equals("dump-csv") || args[0].equals("query"))
            {
                if (args.length == 2)
                {
                    if (CommandBase.doesStringStartWith(args[1], "sort-by-count"))
                    {
                        return ImmutableList.of("sort-by-count");
                    }
                    else
                    {
                        return CommandBase.getListOfStringsMatchingLastWord(args, ForgeRegistries.BLOCKS.getKeys());
                    }
                }
                else
                {
                    return CommandBase.getListOfStringsMatchingLastWord(args, ForgeRegistries.BLOCKS.getKeys());
                }
            }
            else if (args.length >= 3 && args.length <= 8 && args[0].equals("count") && args[1].equals("box"))
            {
                int index = args.length >= 3 && args.length <= 5 ? 2 : 5;
                return CommandBase.getTabCompletionCoordinate(args, index, targetPos);
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

        String cmd = args[0];
        args = dropFirstStrings(args, 1);
        BlockStats blockStats = sender instanceof EntityPlayer ? this.getBlockStatsForPlayer((EntityPlayer) sender) : this.blockStatsConsole;

        // "/tellme blockstats count ..."
        if ((cmd.equals("count") || cmd.equals("count-append")) && args.length >= 1)
        {
            // Possible command formats are:
            // count all-loaded-chunks [dimension]
            // count chunk-radius <radius> [dimension] [x y z (of the center)]
            // count range <x-distance> <y-distance> <z-distance> [dimension] [x y z (of the center)]
            // count box <x1> <y1> <z1> <x2> <y2> <z2> [dimension]
            String type = args[0];
            args = dropFirstStrings(args, 1);
            blockStats.setAppend(cmd.equals("count-append"));

            // Get the world - either the player's current world, or the one based on the provided dimension ID
            World world = this.getWorld(type, args, sender, server);
            BlockPos pos = sender instanceof EntityPlayer ? sender.getPosition() : WorldUtils.getSpawnPoint(world);
            String pre = this.getSubCommandUsagePre();

            // count range <x-distance> <y-distance> <z-distance> [dimension] [x y z (of the center)]
            if (type.equals("range") && (args.length == 3 || args.length == 4 || args.length == 7))
            {
                try
                {
                    if (args.length == 7)
                    {
                        int x = CommandBase.parseInt(args[4]);
                        int y = CommandBase.parseInt(args[5]);
                        int z = CommandBase.parseInt(args[6]);
                        pos = new BlockPos(x, y, z);
                    }

                    int rx = Math.abs(CommandBase.parseInt(args[0]));
                    int ry = Math.abs(CommandBase.parseInt(args[1]));
                    int rz = Math.abs(CommandBase.parseInt(args[2]));

                    this.sendMessage(sender, "Counting blocks...");

                    blockStats.processChunks(world, pos, rx, ry, rz);

                    this.sendMessage(sender, "Done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException(pre + " count range <x-distance> <y-distance> <z-distance> [dimension] [x y z (of the center)]");
                }
            }
            // count box <x1> <y1> <z1> <x2> <y2> <z2> [dimension]
            else if (type.equals("box") && (args.length == 6 || args.length == 7))
            {
                try
                {
                    BlockPos pos1 = parseBlockPos(pos, args, 0, false);
                    BlockPos pos2 = parseBlockPos(pos, args, 3, false);

                    this.sendMessage(sender, "Counting blocks...");

                    blockStats.processChunks(world, pos1, pos2);

                    this.sendMessage(sender, "Done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException("Usage: " + pre + " count box <x1> <y1> <z1> <x2> <y2> <z2> [dimension]");
                }
            }
            // count all-loaded-chunks [dimension]
            else if (type.equals("all-loaded-chunks") && (args.length == 0 || args.length == 1))
            {
                this.sendMessage(sender, "Counting blocks...");

                blockStats.processChunks(TellMe.proxy.getLoadedChunks(world));

                this.sendMessage(sender, "Done");
            }
            // count chunk-radius <radius> [dimension] [x y z (of the center)]
            else if (type.equals("chunk-radius") && (args.length == 1 || args.length == 2 || args.length == 5))
            {
                if (args.length == 5)
                {
                    int x = CommandBase.parseInt(args[2]);
                    int y = CommandBase.parseInt(args[3]);
                    int z = CommandBase.parseInt(args[4]);
                    pos = new BlockPos(x, y, z);
                }

                int radius = 0;

                try
                {
                    radius = Integer.parseInt(args[0]);
                }
                catch (NumberFormatException e)
                {
                    throw new WrongUsageException(pre + " count chunk-radius <radius> [dimension] [x y z (of the center)]");
                }

                int chunkCount = (radius * 2 + 1) * (radius * 2 + 1);

                this.sendMessage(sender, "Loading all the " + chunkCount + " chunks in the given radius of " + radius + " chunks ...");

                List<Chunk> chunks = WorldUtils.loadAndGetChunks(world, pos, radius);

                this.sendMessage(sender, "Counting blocks in the selected " + chunks.size() + " chunks...");

                blockStats.processChunks(chunks);

                this.sendMessage(sender, "Done");
            }
            else
            {
                this.printUsageCount(sender);
                throw new CommandException("Invalid (number of?) arguments!");
            }
        }
        // "/tellme blockstats query ..." or "/tellme blockstats dump ..."
        else if (cmd.equals("query") || cmd.equals("dump") || cmd.equals("dump-csv"))
        {
            List<String> lines;
            Format format = cmd.equals("dump-csv") ? Format.CSV : Format.ASCII;
            boolean sortByCount = args.length >= 1 && args[0].equals("sort-by-count");

            if (sortByCount)
            {
                args = dropFirstStrings(args, 1);
            }

            // We have some filters specified
            if (args.length >= 1)
            {
                lines = blockStats.query(format, Arrays.asList(args), sortByCount);
            }
            else
            {
                lines = blockStats.queryAll(format, sortByCount);
            }

            if (cmd.equals("query"))
            {
                DataDump.printDataToLogger(lines);
                this.sendMessage(sender, "Command output printed to console");
            }
            else
            {
                File file = DataDump.dumpDataToFile("block_stats", lines, format);

                if (file != null)
                {
                    sendClickableLinkMessage(sender, "Output written to file %s", file);
                }
            }
        }
        else
        {
            this.sendMessage(sender, "Usage:");
            this.printUsageCount(sender);
            this.printUsageDump(sender);
            this.printUsageQuery(sender);
        }
    }

    private World getWorld(String countSubCommand, String[] args, ICommandSender sender, MinecraftServer server) throws CommandException
    {
        int index = -1;
        World world = sender.getEntityWorld();

        switch (countSubCommand)
        {
            case "all-loaded-chunks":   index = 0; break;
            case "chunk-radius":        index = 1; break;
            case "range":               index = 3; break;
            case "box":                 index = 6; break;
        }

        if (index >= 0 && args.length > index)
        {
            String dimStr = args[index];

            try
            {
                int dimension = Integer.parseInt(dimStr);
                world = server.getWorld(dimension);
            }
            catch (NumberFormatException e)
            {
                throw new NumberInvalidException("Invalid dimension '%s'", dimStr);
            }

            if (world == null)
            {
                throw new NumberInvalidException("Could not load dimension '%s'", dimStr);
            }
        }

        return world;
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
