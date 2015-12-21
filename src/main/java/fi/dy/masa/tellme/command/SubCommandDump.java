package fi.dy.masa.tellme.command;

import java.io.File;

import fi.dy.masa.tellme.util.DataDump;
import fi.dy.masa.tellme.util.Dump;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

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
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        super.processCommand(sender, args);

        if (args.length == 2)
        {
            if (args[1].equals("blocks") || args[1].equals("items"))
            {
                Dump d = Dump.instance;
                File file;
                if (args[1].equals("blocks"))
                {
                    file = DataDump.dumpDataToFile("block_dump", d.getItemOrBlockDump(d.getItemsOrBlocks(false), false));
                }
                else
                {
                    file = DataDump.dumpDataToFile("item_dump", d.getItemOrBlockDump(d.getItemsOrBlocks(true), true));
                }

                if (file != null)
                {
                	sender.addChatMessage(new ChatComponentText("Output written to file " + file.getName()));
                }
            }
            else if (args[1].equals("entities"))
            {
                Dump d = Dump.instance;
                File file = DataDump.dumpDataToFile("entity_dump", d.getEntityDump());
                if (file != null)
                {
                	sender.addChatMessage(new ChatComponentText("Output written to file " + file.getName()));
                }
            }
        }
    }
}
