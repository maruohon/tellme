package fi.dy.masa.tellme.command;

import java.io.File;
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
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.BiomeLocator;
import fi.dy.masa.tellme.util.WorldUtils;

public class SubCommandBiomeLocate extends SubCommand
{
    private final Map<UUID, BiomeLocator> biomeLocators = Maps.newHashMap();
    private final BiomeLocator biomeLocatorConsole = new BiomeLocator();

    public SubCommandBiomeLocate(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("dump");
        this.subSubCommands.add("dump-csv");
        this.subSubCommands.add("print");
        this.subSubCommands.add("search");
        this.subSubCommands.add("search-append");

        this.addSubCommandHelp("_generic", "Searches for the closest location of biomes around the center point");
        this.addSubCommandHelp("search", "Searches for the closest location of biomes around the center point");
        this.addSubCommandHelp("dump", "Dumps the results from a previous 'search' command into a file in config/tellme/");
        this.addSubCommandHelp("dump-csv", "Dumps the results from a previous 'search' command into a CSV file in config/tellme/");
        this.addSubCommandHelp("query", "Prints the results from a previous 'search' command into the console");
    }

    @Override
    public String getName()
    {
        return "biomelocate";
    }

    private void printUsageSearch(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " search <sampleInterval> <maxSampleRadius> [centerX centerZ] [dimension]"));
    }

    private void printUsageQuery(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " <dump | query>"));
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
                return CommandBase.getListOfStringsMatchingLastWord(args, ForgeRegistries.BIOMES.getKeys());
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
            this.printUsageSearch(sender);
            this.printUsageQuery(sender);
            return;
        }

        super.execute(server, sender, args);

        String cmd = args[0];
        args = dropFirstStrings(args, 1);
        BiomeLocator biomeLocator = sender instanceof EntityPlayer ? this.getBiomeLocatorForPlayer((EntityPlayer) sender) : this.biomeLocatorConsole;

        // "/tellme blockstats count ..."
        if ((cmd.equals("search") || cmd.equals("search-append")) && args.length >= 2 && args.length <= 5)
        {
            // Possible command formats are:
            // search <sampleInterval> <maxSampleRadius> [centerX centerZ] [dimension]
            biomeLocator.setAppend(cmd.equals("search-append"));

            // Get the world - either the player's current world, or the one based on the provided dimension ID
            World world = this.getWorld(cmd, args, sender, server);
            BlockPos pos = sender instanceof EntityPlayer ? sender.getPosition() : WorldUtils.getSpawnPoint(world);
            String pre = this.getSubCommandUsagePre();
            BiomeProvider biomeProvider = world.getBiomeProvider();

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

                this.sendMessage(sender, "Finding closest biome locations...");

                biomeLocator.findClosestBiomePositions(biomeProvider, pos, interval, radius);

                this.sendMessage(sender, "Done");
            }
            catch (NumberInvalidException e)
            {
                throw new WrongUsageException(pre + " count sampled <sampleInterval> <maxSampleRadius> [centerX centerZ] [dimension]");
            }
        }
        else if (cmd.equals("print") || cmd.equals("dump") || cmd.equals("dump-csv"))
        {
            Format format = cmd.equals("dump-csv") ? Format.CSV : Format.ASCII;
            List<String> lines = biomeLocator.getClosestBiomePositions(format);

            if (cmd.equals("print"))
            {
                DataDump.printDataToLogger(lines);
                this.sendMessage(sender, "Command output printed to console");
            }
            else
            {
                File file = DataDump.dumpDataToFile("biome_locations", lines);
                sendClickableLinkMessage(sender, "Output written to file %s", file);
            }
        }
        else
        {
            this.sendMessage(sender, "Usage:");
            this.printUsageSearch(sender);
            this.printUsageQuery(sender);
        }
    }

    private World getWorld(String countSubCommand, String[] args, ICommandSender sender, MinecraftServer server) throws CommandException
    {
        int index = -1;
        World world = sender.getEntityWorld();

        switch (countSubCommand)
        {
            case "search":
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

    private BiomeLocator getBiomeLocatorForPlayer(EntityPlayer player)
    {
        BiomeLocator locator = this.biomeLocators.get(player.getUniqueID());

        if (locator == null)
        {
            locator = new BiomeLocator();
            this.biomeLocators.put(player.getUniqueID(), locator);
        }

        return locator;
    }
}
