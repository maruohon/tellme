package fi.dy.masa.tellme.datadump;

import java.util.List;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;

public class CreativetabDump
{
    public static List<String> getFormattedCreativetabDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(4, format);

        TellMe.proxy.addCreativeTabData(dump);

        dump.addTitle("Index", "Name", "Translated Name", "Icon Item");
        dump.setColumnProperties(0, Alignment.RIGHT, true); // index
        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }
}
