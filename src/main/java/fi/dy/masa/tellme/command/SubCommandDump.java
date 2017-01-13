package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.datadump.BiomeDump;
import fi.dy.masa.tellme.datadump.BlockDump;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.EntityDump;
import fi.dy.masa.tellme.datadump.ItemDump;

public class SubCommandDump extends SubCommand
{
    public SubCommandDump(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.subSubCommands.add("biomes");
        this.subSubCommands.add("blocks");
        this.subSubCommands.add("blocks-with-nbt");
        this.subSubCommands.add("entities");
        this.subSubCommands.add("items");
        this.subSubCommands.add("items-with-nbt");
    }

    @Override
    public String getName()
    {
        return "dump";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length == 2)
        {
            List<String> data = this.getData(args[1]);

            if (data.isEmpty())
            {
                throw new WrongUsageException("tellme.command.error.unknown.parameter", args[1]);
            }

            if (args[0].equals("dump"))
            {
                File file = DataDump.dumpDataToFile(args[1], data);

                if (file != null)
                {
                    this.sendMessage(sender, "tellme.info.output.to.file", file.getName());
                }
            }
            else if (args[0].equals("list"))
            {
                DataDump.printDataToLogger(data);
                this.sendMessage(sender, "tellme.info.output.to.console");
            }
        }
    }

    protected List<String> getData(String type)
    {
        if (type.equals("biomes"))
        {
            return BiomeDump.getFormattedBiomeDump();
        }
        else if (type.equals("blocks"))
        {
            return BlockDump.getFormattedBlockDump(false);
        }
        else if (type.equals("blocks-with-nbt"))
        {
            return BlockDump.getFormattedBlockDump(true);
        }
        else if (type.equals("items"))
        {
            return ItemDump.getFormattedItemDump(false);
        }
        else if (type.equals("items-with-nbt"))
        {
            return ItemDump.getFormattedItemDump(true);
        }
        else if (type.equals("entities"))
        {
            return EntityDump.getFormattedEntityDump();
        }

        return null;
    }
}
