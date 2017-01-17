package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
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
    public static final String[] ENTITIES_3 = new String[] { "by-chunk", "by-type" };
    public static final String[] ENTITIES_4 = new String[] { "dump", "list" };

    public SubCommandLoaded(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("all-entities");
        this.subSubCommands.add("all-tileentities");
        this.subSubCommands.add("dimensions");
        this.subSubCommands.add("entities-in-area");
        this.subSubCommands.add("entities-in-chunk");
        this.subSubCommands.add("tileentities-in-area");
        this.subSubCommands.add("tileentities-in-chunk");
    }

    @Override
    public String getName()
    {
        return "loaded";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 3 && (args[1].equals("dimensions") == false && this.subSubCommands.contains(args[1])))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList(ENTITIES_3));
        }
        else if (args.length == 4 && (args[1].equals("dimensions") == false && this.subSubCommands.contains(args[1])))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList(ENTITIES_4));
        }

        return super.getTabCompletions(server, sender, args);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        String pre = "/" + this.getBaseCommand().getName() + " " + this.getName();

        if (args.length < 2 || this.subSubCommands.contains(args[1]) == false)
        {
            this.sendMessage(sender, "tellme.command.info.usage.noparam");
            //sender.sendMessage(new TextComponentString(pre + " chunks (not implemented yet)"));
            sender.sendMessage(new TextComponentString(pre + " dimensions"));
            sender.sendMessage(new TextComponentString(pre + " all-entities <by-chunk | by-type> <list | dump> [dimension]"));
            sender.sendMessage(new TextComponentString(pre + " all-tileentities <by-chunk | by-type> <list | dump> [dimension]"));
            sender.sendMessage(new TextComponentString(pre + " entities-in-area <by-chunk | by-type> <list | dump> <x-min> <z-min> <x-max> <z-max> [dimension]"));
            sender.sendMessage(new TextComponentString(pre + " tileentities-in-area <by-chunk | by-type> <list | dump> <x-min> <z-min> <x-max> <z-max> [dimension]"));
            sender.sendMessage(new TextComponentString(pre + " entities-in-chunk <by-chunk | by-type> <list | dump> <chunkX> <chunkZ> [dimension]"));
            sender.sendMessage(new TextComponentString(pre + " tileentities-in-chunk <by-chunk | by-type> <list | dump> <chunkX> <chunkZ> [dimension]"));

            return;
        }

        String cmdType = args[1];

        if (cmdType.equals("dimensions") && args.length == 2)
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

        if (cmdType.equals("all-entities") || cmdType.equals("all-tileentities"))
        {
            if (args.length < 4 || args.length > 5)
            {
                this.sendMessage(sender, "tellme.command.info.usage.noparam");
                sender.sendMessage(new TextComponentString(pre + " " + cmdType + " <by-chunk | by-type> <list | dump> [dimension]"));
                return;
            }

            EntityListType type = this.getListType(cmdType, args, 2);
            World world = this.checkAndGetWorld(sender, args, 4);
            data = EntityCountDump.getFormattedEntityCountDumpAll(world, type);
        }
        else if (cmdType.equals("entities-in-area") || cmdType.equals("tileentities-in-area"))
        {
            if (args.length < 8 || args.length > 9)
            {
                this.sendMessage(sender, "tellme.command.info.usage.noparam");
                sender.sendMessage(new TextComponentString(pre + " " + cmdType + " <by-chunk | by-type> <list | dump> <x-min> <z-min> <x-max> <z-max> [dimension]"));
                return;
            }

            EntityListType type = this.getListType(cmdType, args, 2);
            ChunkPos pos1 = new ChunkPos(CommandBase.parseInt(args[4]) >> 4, CommandBase.parseInt(args[5]) >> 4);
            ChunkPos pos2 = new ChunkPos(CommandBase.parseInt(args[6]) >> 4, CommandBase.parseInt(args[7]) >> 4);
            World world = this.checkAndGetWorld(sender, args, 8);
            data = EntityCountDump.getFormattedEntityCountDumpArea(world, type, pos1, pos2);
        }
        else if (cmdType.equals("entities-in-chunk") || cmdType.equals("tileentities-in-chunk"))
        {
            if (args.length < 6 || args.length > 7)
            {
                this.sendMessage(sender, "tellme.command.info.usage.noparam");
                sender.sendMessage(new TextComponentString(pre + " " + cmdType + " <by-chunk | by-type> <list | dump> <chunkX> <chunkZ> [dimension]"));
                return;
            }

            EntityListType type = this.getListType(cmdType, args, 2);
            ChunkPos pos = new ChunkPos(CommandBase.parseInt(args[4]), CommandBase.parseInt(args[5]));
            World world = this.checkAndGetWorld(sender, args, 6);
            data = EntityCountDump.getFormattedEntityCountDumpArea(world, type, pos, pos);
        }

        if (data != null)
        {
            String outputType = args[3];

            if (outputType.equals("list"))
            {
                DataDump.printDataToLogger(data);
                this.sendMessage(sender, "tellme.info.output.to.console");
            }
            else if (outputType.equals("dump"))
            {
                File f = DataDump.dumpDataToFile("loaded_" + cmdType, data);
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

    private EntityListType getListType(String cmdType, String[] args, int indexDataType)
    {
        if (cmdType.contains("tileentities"))
        {
            return args[indexDataType].equals("by-chunk") ? EntityListType.TILEENTITIES_BY_CHUNK : EntityListType.TILEENTITIES_BY_TYPE;
        }
        else
        {
            return args[indexDataType].equals("by-chunk") ? EntityListType.ENTITIES_BY_CHUNK : EntityListType.ENTITIES_BY_TYPE;
        }
    }
}
