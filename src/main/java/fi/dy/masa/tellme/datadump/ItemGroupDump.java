package fi.dy.masa.tellme.datadump;

import java.util.List;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;

public class ItemGroupDump
{
    public static List<String> getFormattedCreativetabDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(4, format);

        TellMe.dataProvider.addItemGroupData(dump);

        dump.addTitle("Index", "Name", "Translated Name", "Icon Item");
        dump.setColumnProperties(0, Alignment.RIGHT, true); // index

        return dump.getLines();
    }
}
