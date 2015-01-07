package fi.dy.masa.tellme.command;

import java.io.File;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import fi.dy.masa.tellme.util.DataDump;
import fi.dy.masa.tellme.util.Dump;

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
                File f;
                if (args[1].equals("blocks"))
                {
                    f = DataDump.dumpDataToFile("block_dump", d.getItemOrBlockDump(d.getItemsOrBlocks(false), false));
                }
                else
                {
                    f = DataDump.dumpDataToFile("item_dump", d.getItemOrBlockDump(d.getItemsOrBlocks(true), true));
                }

                sender.addChatMessage(new ChatComponentText("Output written to file " + f.getName()));
            }
            else if (args[1].equals("entities"))
            {
                Dump d = Dump.instance;
                File f = DataDump.dumpDataToFile("entity_dump", d.getEntityDump());
                sender.addChatMessage(new ChatComponentText("Output written to file " + f.getName()));
            }
        }
    }
}
