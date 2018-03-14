package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
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
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.TileEntityDump;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.chunkprocessor.Locate;
import fi.dy.masa.tellme.util.chunkprocessor.Locate.LocateType;
import fi.dy.masa.tellme.util.chunkprocessor.Locate.OutputType;

public class SubCommandLocate extends SubCommand
{
    public SubCommandLocate(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("block");
        this.subSubCommands.add("entity");
        this.subSubCommands.add("te");

        this.addSubCommandHelp("_generic", "Finds blocks, entities or TileEntities from the world (from loaded chunks - optionally loading chunks first)");
    }

    @Override
    public String getName()
    {
        return "locate";
    }

    private void printUsageLocate(ICommandSender sender)
    {
        String pre = this.getSubCommandUsagePre();
        sender.sendMessage(new TextComponentString(pre + " <block | entity | te> <print | dump[-csv]> all-loaded-chunks [all-dims | dimensionId] <name1 name2 ...>"));
        sender.sendMessage(new TextComponentString(pre + " <block | entity | te> <print | dump[-csv]> chunk-radius <radius> [dimension] [x y z (of the center)] <name1 name2 ...>"));
        sender.sendMessage(new TextComponentString(pre + " <block | entity | te> <print | dump[-csv]> range <x-distance> <y-distance> <z-distance> [dimension] [x y z (of the center)] <name1 name2 ...>"));
        sender.sendMessage(new TextComponentString(pre + " <block | entity | te> <print | dump[-csv]> box <x1> <y1> <z1> <x2> <y2> <z2> [dimension] <name1 name2 ...>"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length < 1)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "block", "entity", "te");
        }
        else if (args.length == 2)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "print", "dump", "dump-csv");
        }
        else if (args.length == 3)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "all-loaded-chunks", "box", "chunk-radius", "range");
        }
        else if (args.length >= 4 && args.length <= 9 && args[2].equals("box"))
        {
            int index = args.length >= 4 && args.length <= 6 ? 3 : 6;
            return CommandBase.getTabCompletionCoordinate(args, index, targetPos);
        }
        // args.length >= 4
        else if (args.length > this.getDimensionArgIndex(args[2]))
        {
            if (args[0].equals("block"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, ForgeRegistries.BLOCKS.getKeys());
            }
            else if (args[0].equals("entity"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, ForgeRegistries.ENTITIES.getKeys());
            }
            else if (args[0].equals("te"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, TileEntityDump.getTileEntityRegistry().getKeys());
            }
        }

        return Collections.emptyList();
    }

    private boolean isValidCommand(String[] args, ICommandSender sender)
    {
        if (args.length < 4)
        {
            this.sendMessage(sender, "Too few arguments");
            return false;
        }

        LocateType locateType = LocateType.fromArg(args[0]);
        OutputType outputType = OutputType.fromArg(args[1]);
        String areaType = args[2];
        //final int dimArgIndex = this.getDimensionArgIndex(areaType);

        if (locateType == LocateType.INVALID || outputType == OutputType.INVALID) // || args.length <= dimArgIndex
        {
            this.sendMessage(sender, "Invalid target type or output type");
            return false;
        }

        //final boolean hasDimArgument = isInteger(args[dimArgIndex]);
        //int requiredArgs = hasDimArgument ? 1 : 0;
        int requiredArgs = this.getDimensionArgIndex(areaType) + 1;

        // requiredArgs == 0 when getDimensionArgIndex() returns -1, which means an invalid areaType
        if (requiredArgs == 0)
        {
            this.sendMessage(sender, "Invalid area type");
            return false;
        }

        if (args.length < requiredArgs)
        {
            this.sendMessage(sender, "Too few arguments for the given area type");
            return false;
        }

        if (areaType.equals("chunk-radius"))
        {
            return isInteger(args[3]) && // "... <radius> ..."
                    ((args.length >= 6 && isInteger(args[5]) == false) || // "... <radius> [dim] <name> ..."
                     (args.length >= 8 && getNumberOfTrailingIntegers(args, 6) == 4) || // "... <radius> <x> <y> <z> <name> ..."
                     (args.length >= 9 && getNumberOfTrailingIntegers(args, 7) == 5));  // "... <radius> <dim> <x> <y> <z> <name> ..."
        }
        else if (areaType.equals("range"))
        {
            return getNumberOfTrailingIntegers(args, 5) == 3 &&                          // "... <x-distance> <y-distance> <z-distance> ..."
                    (isInteger(args[6]) == false ||                                      // "... <x-distance> <y-distance> <z-distance> <name> ..."
                     (args.length >= 8 && isInteger(args[7]) == false) ||                // "... <x-distance> <y-distance> <z-distance> <dim> <name> ..."
                     (args.length >= 10 && getNumberOfTrailingIntegers(args, 8) == 6) || // "... <x-distance> <y-distance> <z-distance> <x> <y> <z> <name> ..."
                     (args.length >= 11 && getNumberOfTrailingIntegers(args, 9) == 7));  // "... <x-distance> <y-distance> <z-distance> <dim> <x> <y> <z> <name> ..."
        }
        else if (areaType.equals("box"))
        {
            return getNumberOfTrailingIntegers(args, 8) == 6 &&                         // "... <x1> <y1> <z1> <x2> <y2> <z2> ..."
                    (isInteger(args[9]) == false ||                                     // "... <x1> <y1> <z1> <x2> <y2> <z2> <name> ..."
                     (args.length >= 11 && getNumberOfTrailingIntegers(args, 9) == 7)); // "... <x1> <y1> <z1> <x2> <y2> <z2> <dim> <name> ..."
        }

        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        //super.execute(server, sender, args);

        if (this.isValidCommand(args, sender) == false)
        {
            this.sendMessage(sender, "Usage:");
            this.printUsageLocate(sender);
            return;
        }

        String typeStr = args[0];
        String outStr = args[1];
        LocateType locateType = LocateType.fromArg(typeStr);
        OutputType outputType = OutputType.fromArg(outStr);
        String areaType = args[2];
        final int dimArgIndex = this.getDimensionArgIndex(areaType);
        int namesStart = dimArgIndex;

        if (args.length > dimArgIndex &&
            (isInteger(args[dimArgIndex]) || (areaType.equals("all-loaded-chunks") && args[dimArgIndex].equals("all-dims"))))
        {
            namesStart += 1;
        }

        // Get the world - either the player's current world, or the one based on the provided dimension ID
        World world = this.getWorld(areaType, args, sender, server);
        BlockPos pos = sender instanceof EntityPlayer ? sender.getPosition() : WorldUtils.getSpawnPoint(world);

        // These have an optional center position argument group
        if ((areaType.equals("chunk-radius") || areaType.equals("range")))
        {
            // dim + pos
            if (args.length >= dimArgIndex + 5 && getNumberOfTrailingIntegers(args, dimArgIndex + 3) >= 4)
            {
                int x = Integer.parseInt(args[dimArgIndex + 1]);
                int y = Integer.parseInt(args[dimArgIndex + 2]);
                int z = Integer.parseInt(args[dimArgIndex + 3]);
                pos = new BlockPos(x, y, z);
                namesStart += 3;
            }
            // pos only
            else if (args.length >= dimArgIndex + 4 && getNumberOfTrailingIntegers(args, dimArgIndex + 2) >= 3)
            {
                int x = Integer.parseInt(args[dimArgIndex]);
                int y = Integer.parseInt(args[dimArgIndex + 1]);
                int z = Integer.parseInt(args[dimArgIndex + 2]);
                pos = new BlockPos(x, y, z);
                namesStart += 2;
            }
        }
        //System.out.printf("len: %d - dai: %d - dim: %d - namesStart: %d - pos: %s\n", args.length, dimArgIndex, world.provider.getDimension(), namesStart, pos);

        Set<String> filters = new HashSet<>();
        //Set<ResourceLocation> validKeys;

        switch (locateType)
        {
            case BLOCK:
                //validKeys = ForgeRegistries.BLOCKS.getKeys();
                break;
            case ENTITY:
                //validKeys = ForgeRegistries.ENTITIES.getKeys();
                break;
            case TILE_ENTITY:
                //validKeys = TileEntityDump.getTileEntityRegistry().getKeys();
                break;
            default:
                this.sendMessage(sender, "Usage:");
                this.printUsageLocate(sender);
                return;
        }

        // /tellme locate <block | entity | te> <print | dump[-csv]> all-loaded-chunks [dimension] <name1 name2 ...>
        // /tellme locate <block | entity | te> <print | dump[-csv]> chunk-radius <radius> [dimension] [x y z (of the center)] <name1 name2 ...>
        // /tellme locate <block | entity | te> <print | dump[-csv]> range <x-distance> <y-distance> <z-distance> [dimension] [x y z (of the center)] <name1 name2 ...>
        // /tellme locate <block | entity | te> <print | dump[-csv]> box <x1> <y1> <z1> <x2> <y2> <z2> [dimension] <name1 name2 ...>

        for (int i = namesStart; i < args.length; i++)
        {
            filters.add(args[i]);
        }

        if (filters.isEmpty())
        {
            this.sendMessage(sender, "Usage:");
            this.printUsageLocate(sender);
            return;
        }

        Locate locate = Locate.create(locateType, outputType, filters);

        // ... range <x-distance> <y-distance> <z-distance> [dimension] [x y z (of the center)] <name1 name2 ...>
        if (areaType.equals("range"))
        {
            try
            {
                int rx = Math.abs(CommandBase.parseInt(args[3]));
                int ry = Math.abs(CommandBase.parseInt(args[4]));
                int rz = Math.abs(CommandBase.parseInt(args[5]));

                this.sendMessage(sender, "Searching...");

                locate.processChunks(world, pos, rx, ry, rz);
                this.outputData(locate, sender);
            }
            catch (NumberInvalidException e)
            {
                throw new WrongUsageException("/tellme locate " + typeStr + " " + outStr + " range <x-distance> <y-distance> <z-distance> [dimension] [x y z (of the center)] <name1 name2 ...>");
            }
        }
        // ... box <x1> <y1> <z1> <x2> <y2> <z2> [dimension] <name1 name2 ...>
        else if (areaType.equals("box"))
        {
            try
            {
                BlockPos pos1 = parseBlockPos(pos, args, 3, false);
                BlockPos pos2 = parseBlockPos(pos, args, 6, false);

                this.sendMessage(sender, "Searching...");

                locate.processChunks(world, pos1, pos2);
                this.outputData(locate, sender);
            }
            catch (NumberInvalidException e)
            {
                throw new WrongUsageException("/tellme locate " + typeStr + " " + outStr + " box <x1> <y1> <z1> <x2> <y2> <z2> [dimension] <name1 name2 ...>");
            }
        }
        // ... all-loaded-chunks [dimension] <name1 name2 ...>
        else if (areaType.equals("all-loaded-chunks"))
        {
            this.sendMessage(sender, "Searching...");

            if (args[3].equals("all-dims"))
            {
                locate.setPrintDimension(true);
                Integer[] ids = DimensionManager.getIDs();

                for (int dim : ids)
                {
                    World worldTmp = DimensionManager.getWorld(dim);

                    if (worldTmp != null)
                    {
                        locate.processChunks(TellMe.proxy.getLoadedChunks(worldTmp));
                    }
                }
            }
            else
            {
                locate.processChunks(TellMe.proxy.getLoadedChunks(world));
            }

            this.outputData(locate, sender);
        }
        // ... chunk-radius <radius> [dimension] [x y z (of the center)] <name1 name2 ...>
        else if (areaType.equals("chunk-radius"))
        {
            int radius = 0;

            try
            {
                radius = Integer.parseInt(args[3]);
            }
            catch (NumberFormatException e)
            {
                throw new WrongUsageException("/tellme locate " + typeStr + " " + outStr + " chunk-radius <radius> [dimension] [x y z (of the center)] <name1 name2 ...>");
            }

            int chunkCount = (radius * 2 + 1) * (radius * 2 + 1);

            this.sendMessage(sender, "Loading all the " + chunkCount + " chunks in the given radius of " + radius + " chunks ...");

            List<Chunk> chunks = WorldUtils.loadAndGetChunks(world, pos, radius);

            this.sendMessage(sender, "Searching in the selected " + chunks.size() + " chunks...");

            locate.processChunks(chunks);
            this.outputData(locate, sender);
        }
    }

    private void outputData(Locate locate, ICommandSender sender)
    {
        List<String> lines = locate.getLines();

        if (locate.getOutputType() == OutputType.PRINT)
        {
            DataDump.printDataToLogger(lines);
            this.sendMessage(sender, "Command output printed to console");
        }
        else
        {
            File file = DataDump.dumpDataToFile("locate_" + locate.getLocateType().toString().toLowerCase(), lines);
            sendClickableLinkMessage(sender, "Output written to file %s", file);
        }
    }

    private World getWorld(String areaType, String[] args, ICommandSender sender, MinecraftServer server) throws CommandException
    {
        int index = this.getDimensionArgIndex(areaType);
        World world = sender.getEntityWorld();

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
            }

            if (world == null)
            {
                throw new CommandException("Could not load the target dimension");
            }
        }

        return world;
    }

    private int getDimensionArgIndex(String areaType)
    {
        int index = -1;

        switch (areaType)
        {
            case "all-loaded-chunks":   index = 3; break;
            case "chunk-radius":        index = 4; break;
            case "range":               index = 6; break;
            case "box":                 index = 9; break;
        }

        return index;
    }
}
