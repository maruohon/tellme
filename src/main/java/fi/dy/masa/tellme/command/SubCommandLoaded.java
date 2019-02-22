package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import fi.dy.masa.tellme.LiteModTellMe;
import fi.dy.masa.tellme.datadump.ChunkDump;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.datadump.EntityCountDump;
import fi.dy.masa.tellme.datadump.EntityCountDump.EntityListType;
import fi.dy.masa.tellme.util.WorldUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class SubCommandLoaded extends SubCommand
{
    public SubCommandLoaded(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("chunks");
        this.subSubCommands.add("dimensions");
        this.subSubCommands.add("entities-all");
        this.subSubCommands.add("entities-in-area");
        this.subSubCommands.add("entities-in-chunk");
        this.subSubCommands.add("tileentities-all");
        this.subSubCommands.add("tileentities-in-area");
        this.subSubCommands.add("tileentities-in-chunk");

        this.addSubCommandUsage("chunks",                   "chunks <list | dump> [dimension]");
        this.addSubCommandUsage("dimensions",               "dimensions");
        this.addSubCommandUsage("entities-all",             "entities-all <all | by-chunk | by-type> <list | dump> [dimension]");
        this.addSubCommandUsage("entities-in-area",         "entities-in-area <all | by-chunk | by-type> <list | dump> <x-min> <z-min> <x-max> <z-max> [dimension]");
        this.addSubCommandUsage("entities-in-chunk",        "entities-in-chunk <all | by-chunk | by-type> <list | dump> <chunkX> <chunkZ> [dimension]");
        this.addSubCommandUsage("tileentities-all",         "tileentities-all <all | by-chunk | by-type> <list | dump> [dimension]");
        this.addSubCommandUsage("tileentities-in-area",     "tileentities-in-area <all | by-chunk | by-type> <list | dump> <x-min> <z-min> <x-max> <z-max> [dimension]");
        this.addSubCommandUsage("tileentities-in-chunk",    "tileentities-in-chunk <all | by-chunk | by-type> <list | dump> <chunkX> <chunkZ> [dimension]");
    }

    @Override
    public String getName()
    {
        return "loaded";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        String cmd = args[0];

        if (cmd.equals("dimensions") == false && this.subSubCommands.contains(cmd))
        {
            if ((args.length == 3 && cmd.equals("chunks") == false) || (args.length == 2 && cmd.equals("chunks")))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "dump", "list");
            }
            else if (args.length == 2)
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "all", "by-chunk", "by-type");
            }
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String pre = "/" + this.getBaseCommand().getName() + " " + this.getName() + " ";

        if (args.length < 1 || this.subSubCommands.contains(args[0]) == false || args[0].equals("help"))
        {
            this.sendMessage(sender, "Usage:");

            for (String cmd : this.subSubCommands)
            {
                if (cmd.equals("help") == false)
                {
                    sender.sendMessage(new TextComponentString(pre + this.getSubCommandUsage(cmd)));
                }
            }

            return;
        }

        String cmd = args[0];
        int outputTypeArgIndex = 2;

        if (cmd.equals("dimensions") && args.length == 1)
        {
            for (World world : server.worlds)
            {
                if (world != null)
                {
                    LiteModTellMe.logger.info(String.format("DIM %4d: %-16s [%4d loaded chunks, %4d loaded entities, %d players]",
                            fi.dy.masa.malilib.util.WorldUtils.getDimensionId(world), world.provider.getDimensionType().getName(),
                            WorldUtils.getLoadedChunkCount(world),
                            world.loadedEntityList.size(), world.playerEntities.size()));
                }
            }

            this.sendMessage(sender, "Command output printed to console");
            return;
        }

        List<String> data = null;

        if (cmd.equals("chunks") && (args.length == 2 || args.length == 3))
        {
            outputTypeArgIndex = 1;
            Integer dim = args.length == 3 ? CommandBase.parseInt(args[2]) : null;
            data = ChunkDump.getFormattedChunkDump(Format.ASCII, dim, server);
        }
        else if ((cmd.equals("entities-all") || cmd.equals("tileentities-all")) && (args.length == 3 || args.length == 4))
        {
            EntityListType type = this.getListType(cmd, args[1]);
            World world = this.checkAndGetWorld(server, sender, args, 3);
            data = EntityCountDump.getFormattedEntityCountDumpAll(world, type);
        }
        else if ((cmd.equals("entities-in-area") || cmd.equals("tileentities-in-area")) && (args.length == 7 || args.length == 8))
        {
            EntityListType type = this.getListType(cmd, args[2]);
            Entity senderEntity = sender.getCommandSenderEntity();
            ChunkPos pos1;
            ChunkPos pos2;

            if (senderEntity != null)
            {
                Vec3d senderPos = senderEntity.getPositionVector();
                pos1 = new ChunkPos(((int) CommandBase.parseCoordinate(senderPos.x, args[3], false).getResult()) >> 4,
                                    ((int) CommandBase.parseCoordinate(senderPos.z, args[4], false).getResult()) >> 4);
                pos2 = new ChunkPos(((int) CommandBase.parseCoordinate(senderPos.x, args[5], false).getResult()) >> 4,
                                    ((int) CommandBase.parseCoordinate(senderPos.z, args[6], false).getResult()) >> 4);
            }
            else
            {
                pos1 = new ChunkPos(CommandBase.parseInt(args[3]) >> 4, CommandBase.parseInt(args[4]) >> 4);
                pos2 = new ChunkPos(CommandBase.parseInt(args[5]) >> 4, CommandBase.parseInt(args[6]) >> 4);
            }

            World world = this.checkAndGetWorld(server, sender, args, 7);
            data = EntityCountDump.getFormattedEntityCountDumpArea(world, type, pos1, pos2);
        }
        else if ((cmd.equals("entities-in-chunk") || cmd.equals("tileentities-in-chunk")) && (args.length == 5 || args.length == 6))
        {
            EntityListType type = this.getListType(cmd, args[1]);
            Entity senderEntity = sender.getCommandSenderEntity();
            ChunkPos pos;

            if (senderEntity != null)
            {
                Vec3d senderPos = senderEntity.getPositionVector();
                pos = new ChunkPos(((int) CommandBase.parseCoordinate(senderPos.x, args[3], false).getResult()),
                                   ((int) CommandBase.parseCoordinate(senderPos.z, args[4], false).getResult()));
            }
            else
            {
                pos = new ChunkPos(CommandBase.parseInt(args[3]), CommandBase.parseInt(args[4]));
            }

            World world = this.checkAndGetWorld(server, sender, args, 5);
            data = EntityCountDump.getFormattedEntityCountDumpArea(world, type, pos, pos);
        }

        if (data != null)
        {
            String outputType = args[outputTypeArgIndex];

            if (outputType.equals("list"))
            {
                DataDump.printDataToLogger(data);
                this.sendMessage(sender, "Command output printed to console");
            }
            else if (outputType.equals("dump"))
            {
                File file = DataDump.dumpDataToFile("loaded_" + cmd, data);
                sendClickableLinkMessage(sender, "Output written to file %s", file);
            }
            else
            {
                throw new WrongUsageException("Unrecognized parameter: '" + outputType + "'");
            }
        }
        else
        {
            this.sendMessage(sender, "Usage:");
            sender.sendMessage(new TextComponentString(pre + this.getSubCommandUsage(cmd)));
        }
    }

    private World checkAndGetWorld(MinecraftServer server, ICommandSender sender, String[] args, int index) throws CommandException
    {
        World world = sender.getEntityWorld();

        if (args.length >= (index + 1))
        {
            int dimension = CommandBase.parseInt(args[index]);
            world = server.getWorld(dimension);
        }

        if (world == null)
        {
            throw new WrongUsageException("The requested world is not currently loaded");
        }

        return world;
    }

    private EntityListType getListType(String cmd, String arg)
    {
        if (cmd.contains("tileentities"))
        {
            if (arg.equals("by-chunk"))
            {
                return EntityListType.TILE_ENTITIES_BY_CHUNK;
            }
            else if (arg.equals("by-type"))
            {
                return EntityListType.TILE_ENTITIES_BY_TYPE;
            }

            return EntityListType.ALL_TILE_ENTITIES;
        }
        else
        {
            if (arg.equals("by-chunk"))
            {
                return EntityListType.ENTITIES_BY_CHUNK;
            }
            else if (arg.equals("by-type"))
            {
                return EntityListType.ENTITIES_BY_TYPE;
            }

            return EntityListType.ALL_ENTITIES;
        }
    }
}
