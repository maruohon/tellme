package fi.dy.masa.tellme.command;

import java.io.File;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import fi.dy.masa.tellme.util.DataDump;

public class SubCommandDump extends SubCommand
{
    public SubCommandDump()
    {
        super();
        this.subSubCommands.add("blocks");
        this.subSubCommands.add("entities");
        this.subSubCommands.add("items");
    }

    @Override
    public String getCommandName()
    {
        return "dump";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length == 2)
        {
            if (args[1].equals("blocks"))
            {
                File file = DataDump.dumpDataToFile("block_dump", new DataDump().getFormattedBlockDump());
                if (file != null)
                {
                    sender.addChatMessage(new TextComponentString("Output written to file " + file.getName()));
                }
            }
            else if (args[1].equals("items"))
            {
                File file = DataDump.dumpDataToFile("item_dump", new DataDump().getFormattedItemDump());
                if (file != null)
                {
                    sender.addChatMessage(new TextComponentString("Output written to file " + file.getName()));
                }
            }
            else if (args[1].equals("entities"))
            {
                File file = DataDump.dumpDataToFile("entity_dump", new DataDump().getEntityDump());
                if (file != null)
                {
                    sender.addChatMessage(new TextComponentString("Output written to file " + file.getName()));
                }
            }
        }
    }
}
