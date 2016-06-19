package fi.dy.masa.tellme.command;

import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.DataDump;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.EntityInfo.EntityListType;

public class SubCommandLoaded extends SubCommand
{
    public static final String[] ENTITIES_2 = new String[] { "chunk", "type" };
    public static final String[] ENTITIES_3 = new String[] { "dump", "list" };

    public SubCommandLoaded()
    {
        super();

        this.subSubCommands.add("dimensions");
        this.subSubCommands.add("entities");
        this.subSubCommands.add("tileentities");
    }

    @Override
    public String getCommandName()
    {
        return "loaded";
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 3 && args[1].equals("entities"))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList(ENTITIES_2));
        }
        else if (args.length == 4 && args[1].equals("entities"))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, Arrays.asList(ENTITIES_3));
        }

        return super.getTabCompletionOptions(server, sender, args);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length < 2)
        {
            String pre = "/" + CommandTellme.instance.getCommandName() + " " + this.getCommandName();

            sender.addChatMessage(new TextComponentString(I18n.translateToLocal("info.command.usage") + ": "));
            sender.addChatMessage(new TextComponentString(pre + " dimensions (not implemented yet)"));
            sender.addChatMessage(new TextComponentString(pre + " entities <chunk | type> <list | dump> [dimension]"));
            sender.addChatMessage(new TextComponentString(pre + " tileentities (not implemented yet)"));

            return;
        }

        if (args[1].equals("dimensions") && args.length == 2)
        {
        }
        else if (args[1].equals("entities"))
        {
            if (args.length < 4)
            {
                String pre = "/" + CommandTellme.instance.getCommandName() + " " + this.getCommandName();

                sender.addChatMessage(new TextComponentString(I18n.translateToLocal("info.command.usage") + ": "));
                sender.addChatMessage(new TextComponentString(pre + " entities <chunk | type> <list | dump> [dimension]"));

                return;
            }

            World world;
            if (args.length == 5)
            {
                int dimension = CommandBase.parseInt(args[4]);
                world = DimensionManager.getWorld(dimension);
            }
            else
            {
                world = sender.getEntityWorld();
            }

            if (world == null)
            {
                TellMe.logger.info("The requested world is not currently loaded");
                return;
            }

            EntityListType type = args[2].equals("chunk") ? EntityListType.BY_CHUNK : EntityListType.BY_ENTITY_TYPE;
            if (args[3].equals("list"))
            {
                for (String line : EntityInfo.getEntityCounts(world, type))
                {
                    TellMe.logger.info(line);
                }
            }
            else if (args[3].equals("dump"))
            {
                DataDump.dumpDataToFile("loaded_entities", EntityInfo.getEntityCounts(world, type));
            }
        }
        else if (args[1].equals("tileentities"))
        {
        }
    }
}
