package fi.dy.masa.tellme.command;

import java.io.File;
import javax.annotation.Nullable;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.datadump.BlockDump;
import fi.dy.masa.tellme.datadump.DataDump;

public class SubCommandDumpJson extends SubCommandDump
{
    public SubCommandDumpJson(CommandTellme baseCommand)
    {
        super(baseCommand);
    }

    @Override
    protected void addSubSubCommands()
    {
        this.subSubCommands.add("blocks");
    }

    @Override
    public String getName()
    {
        return "dump-json";
    }

    @Override
    protected void outputData(MinecraftServer server, ICommandSender sender, String arg) throws CommandException
    {
        String data = this.getData(arg);

        if (data == null)
        {
            throw new CommandException("Unrecognized parameter: '" + arg + "'");
        }

        File file = DataDump.dumpDataToFile(arg, Lists.newArrayList(data));

        if (file != null)
        {
            sendClickableLinkMessage(sender, "Output written to file %s", file);
        }
    }

    @Nullable
    private String getData(String type)
    {
        switch (type)
        {
            case "blocks":  return BlockDump.getJsonBlockDump();
            default:
        }

        return null;
    }
}
