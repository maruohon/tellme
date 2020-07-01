package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class CommandDump
{
    public static List<String> getFormattedCommandDump(DataDump.Format format, @Nullable MinecraftServer server)
    {
        DataDump dump = new DataDump(2, format);

        TellMe.dataProvider.addCommandDumpData(dump, server);

        dump.addTitle("Command", "Class");

        dump.addFooter("'-' in the Class column means that there is no");
        dump.addFooter("executable command at that top-level node of that");
        dump.addFooter("command (ie. it requires more arguments).");
        return dump.getLines();
    }
}
