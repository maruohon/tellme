package fi.dy.masa.tellme.datadump;

import java.util.List;
import fi.dy.masa.tellme.TellMe;

public class CommandDump
{
    public static List<String> getFormattedCommandDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(2, format);

        TellMe.dataProvider.addCommandDumpData(dump);

        dump.addTitle("Command", "Class");

        dump.addFooter("'-' in the Class column means that there is no");
        dump.addFooter("executable command at that top-level node of that");
        dump.addFooter("command (ie. it requires more arguments).");
        return dump.getLines();
    }
}
