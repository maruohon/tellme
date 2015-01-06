package fi.dy.masa.tellme.command;

import java.io.File;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import fi.dy.masa.tellme.util.BiomeInfo;
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
    public void processCommand(ICommandSender sender, String[] args)
    {
        super.processCommand(sender, args);

        if (args.length == 2)
        {
            if (args[1].equals("blocks"))
            {
                Dump d = Dump.instance;
                File f = DataDump.dumpDataToFile("block_dump", d.getDump(d.getBlocks()));
                sender.addChatMessage(new ChatComponentText("Output written to file " + f.getName()));
            }
            else if (args[1].equals("entities"))
            {
                Dump d = Dump.instance;
                File f = DataDump.dumpDataToFile("entity_dump", BiomeInfo.getBiomeList());
                sender.addChatMessage(new ChatComponentText("Output written to file " + f.getName()));
            }
            else if (args[1].equals("items"))
            {
                Dump d = Dump.instance;
                File f = DataDump.dumpDataToFile("item_dump", d.getDump(d.getItems()));
                sender.addChatMessage(new ChatComponentText("Output written to file " + f.getName()));
            }
        }
    }
}
