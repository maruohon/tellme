package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.EntityCountDump;
import fi.dy.masa.tellme.datadump.EntityCountDump.EntityListType;
import fi.dy.masa.tellme.util.WorldUtils;

public class SubCommandLoaded extends SubCommand
{
    private final Map<String, String> usage = new HashMap<String, String>();

    public SubCommandLoaded(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("dimensions");
        this.subSubCommands.add("entities-all");
        this.subSubCommands.add("entities-in-area");
        this.subSubCommands.add("entities-in-chunk");
        this.subSubCommands.add("tileentities-all");
        this.subSubCommands.add("tileentities-in-area");
        this.subSubCommands.add("tileentities-in-chunk");

        this.usage.put("dimensions",            "dimensions");
        this.usage.put("entities-all",          "entities-all <all | by-chunk | by-type> <list | dump> [dimension]");
        this.usage.put("entities-in-area",      "entities-in-area <all | by-chunk | by-type> <list | dump> <x-min> <z-min> <x-max> <z-max> [dimension]");
        this.usage.put("entities-in-chunk",     "entities-in-chunk <all | by-chunk | by-type> <list | dump> <chunkX> <chunkZ> [dimension]");
        this.usage.put("tileentities-all",      "tileentities-all <by-chunk | by-type> <list | dump> [dimension]");
        this.usage.put("tileentities-in-area",  "tileentities-in-area <by-chunk | by-type> <list | dump> <x-min> <z-min> <x-max> <z-max> [dimension]");
        this.usage.put("tileentities-in-chunk", "tileentities-in-chunk <by-chunk | by-type> <list | dump> <chunkX> <chunkZ> [dimension]");
    }

    @Override
    public String getName()
    {
        return "loaded";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        String cmd = args[0];

        if (args.length == 2 && cmd.startsWith("tileentities"))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "by-chunk", "by-type");
        }
        else if (args.length == 2 && cmd.startsWith("entities"))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "all", "by-chunk", "by-type");
        }
        else if (args.length == 3 && cmd.contains("entities"))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "dump", "list");
        }

        return super.getTabCompletions(server, sender, args);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        String pre = "/" + this.getBaseCommand().getName() + " " + this.getName() + " ";

        if (args.length < 1 || this.subSubCommands.contains(args[0]) == false)
        {
            this.sendMessage(sender, "tellme.command.info.usage.noparam");
            //sender.sendMessage(new TextComponentString(pre + " chunks (not implemented yet)"));

            for (String cmd : this.subSubCommands)
            {
                sender.sendMessage(new TextComponentString(pre + this.usage.get(cmd)));
            }

            return;
        }

        String cmd = args[0];

        if (cmd.equals("dimensions") && args.length == 1)
        {
            Integer[] dims = DimensionManager.getIDs();

            for (int id : dims)
            {
                World world = DimensionManager.getWorld(id);

                if (world != null)
                {
                    TellMe.logger.info(String.format("DIM %4d: %-16s [%4d loaded chunks, %4d loaded entities]",
                            id, world.provider.getDimensionType().getName(), WorldUtils.getLoadedChunkCount(world), world.loadedEntityList.size()));
                }
            }

            this.sendMessage(sender, "tellme.info.output.to.console");
            return;
        }

        List<String> data = null;

        if (cmd.equals("entities-all") || cmd.equals("tileentities-all"))
        {
            if (args.length < 3 || args.length > 4)
            {
                this.sendMessage(sender, "tellme.command.info.usage.noparam");
                sender.sendMessage(new TextComponentString(pre + this.usage.get(cmd)));
                return;
            }

            EntityListType type = this.getListType(cmd, args[1]);
            World world = this.checkAndGetWorld(sender, args, 3);
            data = EntityCountDump.getFormattedEntityCountDumpAll(world, type);
        }
        else if (cmd.equals("entities-in-area") || cmd.equals("tileentities-in-area"))
        {
            if (args.length < 7 || args.length > 8)
            {
                this.sendMessage(sender, "tellme.command.info.usage.noparam");
                sender.sendMessage(new TextComponentString(pre + this.usage.get(cmd)));
                return;
            }

            EntityListType type = this.getListType(cmd, args[2]);
            Entity senderEntity = sender.getCommandSenderEntity();
            ChunkPos pos1;
            ChunkPos pos2;

            if (senderEntity != null)
            {
                Vec3d senderPos = senderEntity.getPositionVector();
                pos1 = new ChunkPos(((int) CommandBase.parseCoordinate(senderPos.xCoord, args[3], false).getResult()) >> 4,
                                    ((int) CommandBase.parseCoordinate(senderPos.zCoord, args[4], false).getResult()) >> 4);
                pos2 = new ChunkPos(((int) CommandBase.parseCoordinate(senderPos.xCoord, args[5], false).getResult()) >> 4,
                                    ((int) CommandBase.parseCoordinate(senderPos.zCoord, args[6], false).getResult()) >> 4);
            }
            else
            {
                pos1 = new ChunkPos(CommandBase.parseInt(args[3]) >> 4, CommandBase.parseInt(args[4]) >> 4);
                pos2 = new ChunkPos(CommandBase.parseInt(args[5]) >> 4, CommandBase.parseInt(args[6]) >> 4);
            }

            World world = this.checkAndGetWorld(sender, args, 7);
            data = EntityCountDump.getFormattedEntityCountDumpArea(world, type, pos1, pos2);
        }
        else if (cmd.equals("entities-in-chunk") || cmd.equals("tileentities-in-chunk"))
        {
            if (args.length < 5 || args.length > 6)
            {
                this.sendMessage(sender, "tellme.command.info.usage.noparam");
                sender.sendMessage(new TextComponentString(pre + this.usage.get(cmd)));
                return;
            }

            EntityListType type = this.getListType(cmd, args[1]);
            Entity senderEntity = sender.getCommandSenderEntity();
            ChunkPos pos;

            if (senderEntity != null)
            {
                Vec3d senderPos = senderEntity.getPositionVector();
                pos = new ChunkPos(((int) CommandBase.parseCoordinate(senderPos.xCoord, args[3], false).getResult()) >> 4,
                                   ((int) CommandBase.parseCoordinate(senderPos.zCoord, args[4], false).getResult()) >> 4);
            }
            else
            {
                pos = new ChunkPos(CommandBase.parseInt(args[3]), CommandBase.parseInt(args[4]));
            }

            World world = this.checkAndGetWorld(sender, args, 5);
            data = EntityCountDump.getFormattedEntityCountDumpArea(world, type, pos, pos);
        }

        if (data != null)
        {
            String outputType = args[2];

            if (outputType.equals("list"))
            {
                DataDump.printDataToLogger(data);
                this.sendMessage(sender, "tellme.info.output.to.console");
            }
            else if (outputType.equals("dump"))
            {
                File f = DataDump.dumpDataToFile("loaded_" + cmd, data);
                this.sendMessage(sender, "tellme.info.output.to.file", f.getName());
            }
            else
            {
                throw new WrongUsageException("tellme.command.error.unknown.parameter", outputType);
            }
        }
    }

    private World checkAndGetWorld(ICommandSender sender, String[] args, int index) throws CommandException
    {
        World world;

        if (args.length >= (index + 1))
        {
            int dimension = CommandBase.parseInt(args[index]);
            world = DimensionManager.getWorld(dimension);
        }
        else
        {
            world = sender.getEntityWorld();
        }

        if (world == null)
        {
            throw new WrongUsageException("tellme.command.error.world.not.loaded");
        }

        return world;
    }

    private EntityListType getListType(String cmd, String arg)
    {
        if (cmd.contains("tileentities"))
        {
            if (arg.equals("by-chunk"))
            {
                return EntityListType.TILEENTITIES_BY_CHUNK;
            }

            return EntityListType.TILEENTITIES_BY_TYPE;
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
