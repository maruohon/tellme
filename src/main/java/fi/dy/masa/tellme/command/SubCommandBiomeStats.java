package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.chunkprocessor.BiomeStats;

public class SubCommandBiomeStats extends SubCommand
{
    private final Map<UUID, BiomeStats> biomeStats = Maps.newHashMap();
    private final BiomeStats biomeStatsConsole = new BiomeStats();

    public SubCommandBiomeStats(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("count");
        this.subSubCommands.add("count-append");
        this.subSubCommands.add("dump");
        this.subSubCommands.add("dump-csv");
        this.subSubCommands.add("query");

        this.addSubCommandHelp("_generic", "Calculates the number of x/z columns with each biome in a given area");
        this.addSubCommandHelp("count", "Counts all the biomes in the given area");
        this.addSubCommandHelp("dump", "Dumps the stats from a previous 'count' command into a file in config/tellme/");
        this.addSubCommandHelp("dump-csv", "Dumps the stats from a previous 'count' command into a CSV file in config/tellme/");
        this.addSubCommandHelp("query", "Prints the stats from a previous 'count' command into the console");
    }

    @Override
    public String getName()
    {
        return "biomestats";
    }

    private void printUsageCount(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " count[-append] area <x1> <z1> <x2> <z2> [dimension]"));
        sender.sendMessage(new TextComponentString(pre + " count[-append] chunk-radius <radius> [x z (of the center)] [dimension]"));
        sender.sendMessage(new TextComponentString(pre + " count[-append] range <x-distance> <z-distance> [x z (of the center)] [dimension]"));
        sender.sendMessage(new TextComponentString(pre + " count[-append] sampled <sampleInterval> <sampleRadius> [centerX centerZ] [dimension]"));
    }

    private void printUsageQuery(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " <dump | query> [modid:biome modid:biome ...]"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.subSubCommands);
        }
        else if (args.length == 2)
        {
            if (args[0].equals("dump") || args[0].equals("dump-csv") || args[0].equals("query"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, Biome.REGISTRY.getKeys());
            }
            else if (args[0].equals("count"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "area", "chunk-radius", "range", "sampled");
            }
        }
        else if (args.length >= 3 && args[0].equals("count"))
        {
            if (args.length <= 6 && args[1].equals("area"))
            {
                int index = args.length <= 4 ? 2 : 4;
                return CommandBase.getTabCompletionCoordinateXZ(args, index, targetPos);
            }
            else if (args.length >= 4 && args.length <= 5 && args[1].equals("chunk-radius"))
            {
                return CommandBase.getTabCompletionCoordinateXZ(args, 3, targetPos);
            }
            else if (args.length >= 5 && args.length <= 6 && args[1].equals("range"))
            {
                return CommandBase.getTabCompletionCoordinateXZ(args, 4, targetPos);
            }
            else if (args.length >= 5 && args.length <= 6 && args[1].equals("sampled"))
            {
                return CommandBase.getTabCompletionCoordinateXZ(args, 4, targetPos);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            this.sendMessage(sender, "Usage:");
            this.printUsageCount(sender);
            this.printUsageQuery(sender);
            return;
        }

        super.execute(server, sender, args);

        String cmd = args[0];
        BiomeStats biomeStats = sender instanceof EntityPlayer ? this.getBiomeStatsForPlayer((EntityPlayer) sender) : this.biomeStatsConsole;

        // "/tellme blockstats count ..."
        if ((cmd.equals("count") || cmd.equals("count-append")) && args.length >= 2)
        {
            // Possible command formats are:
            // count area <x1> <z1> <x2> <z2> [dimension]
            // count chunk-radius <radius> [x z (of the center)] [dimension]
            // count range <x-distance> <z-distance> [x z (of the center)] [dimension]
            String type = args[1];
            args = dropFirstStrings(args, 2);
            biomeStats.setAppend(cmd.equals("count-append"));

            // Get the world - either the player's current world, or the one based on the provided dimension ID
            World world = this.getWorld(type, args, sender, server);
            BlockPos pos = sender instanceof EntityPlayer ? sender.getPosition() : WorldUtils.getSpawnPoint(world);
            String pre = this.getSubCommandUsagePre();
            BiomeProvider biomeProvider = world.getBiomeProvider();

            // count range <x-distance> <z-distance> [x z (of the center)] [dimension]
            if (type.equals("range") && (args.length >= 2 && args.length <= 5))
            {
                try
                {
                    if (args.length >= 4)
                    {
                        pos = parseBlockPosXZ(pos, args, 2, false);
                    }

                    int rx = Math.abs(CommandBase.parseInt(args[0]));
                    int rz = Math.abs(CommandBase.parseInt(args[1]));

                    this.sendMessage(sender, "Counting biomes...");

                    biomeStats.getFullBiomeDistribution(biomeProvider, pos.add(-rx, 0, -rz), pos.add(rx, 0, rz));

                    this.sendMessage(sender, "Done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException(pre + " count range <x-distance> <z-distance> [x z (of the center)] [dimension]");
                }
            }
            // count area <x1> <z1> <x2> <z2> [dimension]
            else if (type.equals("area") && (args.length == 4 || args.length == 5))
            {
                try
                {
                    double x1 = CommandBase.parseDouble(pos.getX(), args[0], -30000000, 30000000, false);
                    double z1 = CommandBase.parseDouble(pos.getZ(), args[1], -30000000, 30000000, false);
                    double x2 = CommandBase.parseDouble(pos.getX(), args[2], -30000000, 30000000, false);
                    double z2 = CommandBase.parseDouble(pos.getZ(), args[3], -30000000, 30000000, false);
                    BlockPos pos1 = new BlockPos(Math.min(x1, x2), 0, Math.min(z1, z2));
                    BlockPos pos2 = new BlockPos(Math.max(x1, x2), 0, Math.max(z1, z2));

                    this.sendMessage(sender, "Counting biomes...");

                    biomeStats.getFullBiomeDistribution(biomeProvider, pos1, pos2);

                    this.sendMessage(sender, "Done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException("Usage: " + pre + " count area <x1> <z1> <x2> <z2> [dimension]");
                }
            }
            // count chunk-radius <radius> [x z (of the center)] [dimension]
            else if (type.equals("chunk-radius") && (args.length >= 1 && args.length <= 4))
            {
                if (args.length == 4)
                {
                    pos = parseBlockPosXZ(pos, args, 1, false);
                }

                int radius = 0;

                try
                {
                    radius = Integer.parseInt(args[0]);
                }
                catch (NumberFormatException e)
                {
                    throw new WrongUsageException(pre + " count chunk-radius <radius> [x y z (of the center)] [dimension]");
                }

                int chunkCount = (radius * 2 + 1) * (radius * 2 + 1);
                this.sendMessage(sender, "Counting biomes in the selected " + chunkCount + " chunks...");

                biomeStats.getFullBiomeDistribution(biomeProvider, pos.add(-radius * 16, 0, -radius * 16), pos.add(radius * 16, 0, radius * 16));

                this.sendMessage(sender, "Done");
            }
            // count sampled <sampleInterval> <sampleRadius> [centerX centerZ] [dimension]
            else if (type.equals("sampled") && (args.length >= 2 && args.length <= 5))
            {
                try
                {
                    if (args.length >= 4)
                    {
                        pos = parseBlockPosXZ(pos, args, 2, false);
                    }

                    int interval = CommandBase.parseInt(args[0]);
                    int radius = CommandBase.parseInt(args[1]);

                    if (interval <= 0)
                    {
                        new NumberInvalidException("Interval must be a positive integer number");
                    }

                    if (radius < 0)
                    {
                        new NumberInvalidException("Radius must be a positive integer number or 0");
                    }

                    this.sendMessage(sender, "Counting biomes...");

                    biomeStats.getSampledBiomeDistribution(biomeProvider, pos.getX(), pos.getZ(), interval, radius);

                    this.sendMessage(sender, "Done");
                }
                catch (NumberInvalidException e)
                {
                    throw new WrongUsageException(pre + " count sampled <sampleInterval> <sampleRadius> [centerX centerZ] [dimension]");
                }
            }
            else
            {
                this.printUsageCount(sender);
                throw new CommandException("Invalid (number of?) arguments!");
            }
        }
        else if (cmd.equals("query") || cmd.equals("dump") || cmd.equals("dump-csv"))
        {
            List<String> lines;
            Format format = cmd.equals("dump-csv") ? Format.CSV : Format.ASCII;

            // We have some filters specified
            if (args.length > 1)
            {
                lines = biomeStats.query(format, Arrays.asList(dropFirstStrings(args, 1)));
            }
            else
            {
                lines = biomeStats.queryAll(format);
            }

            if (cmd.equals("query"))
            {
                DataDump.printDataToLogger(lines);
                this.sendMessage(sender, "Command output printed to console");
            }
            else
            {
                File file = DataDump.dumpDataToFile("biome_stats", lines, format);

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
            this.printUsageQuery(sender);
        }
    }

    private World getWorld(String countSubCommand, String[] args, ICommandSender sender, MinecraftServer server) throws CommandException
    {
        int index = -1;
        World world = sender.getEntityWorld();

        switch (countSubCommand)
        {
            case "area":
                index = 4;
                break;
            case "chunk-radius":
                if (args.length == 4)
                    index = 3;
                else if (args.length == 2)
                    index = 1;
                break;
            case "range":
            case "sampled":
                if (args.length == 5)
                    index = 4;
                else if (args.length == 3)
                    index = 2;
                break;
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

    private BiomeStats getBiomeStatsForPlayer(EntityPlayer player)
    {
        BiomeStats stats = this.biomeStats.get(player.getUniqueID());

        if (stats == null)
        {
            stats = new BiomeStats();
            this.biomeStats.put(player.getUniqueID(), stats);
        }

        return stats;
    }
}
