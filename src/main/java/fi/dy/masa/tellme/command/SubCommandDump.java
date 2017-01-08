package fi.dy.masa.tellme.command;

import java.io.File;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.util.DataDump;

public class SubCommandDump extends SubCommand
{
    public SubCommandDump(CommandTellme baseCommand)
    {
        super(baseCommand);
        this.subSubCommands.add("blocks");
        this.subSubCommands.add("entities");
        this.subSubCommands.add("items");
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
            File file = null;

            if (args[1].equals("blocks"))
            {
                file = DataDump.dumpDataToFile("block_dump", DataDump.getFormattedBlockDump());
            }
            else if (args[1].equals("items"))
            {
                file = DataDump.dumpDataToFile("item_dump", DataDump.getFormattedItemDump());
            }
            else if (args[1].equals("entities"))
            {
                file = DataDump.dumpDataToFile("entity_dump", DataDump.getEntityDump());
            }

            if (file != null)
            {
                this.sendMessage(sender, "tellme.info.output.to.file", file.getName());
            }
        }
    }
}
