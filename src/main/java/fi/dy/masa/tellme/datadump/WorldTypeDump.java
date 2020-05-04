package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.world.WorldType;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;

public class WorldTypeDump
{
    public static List<String> getFormattedWorldTypeDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(4, format);

        for (WorldType type : WorldType.WORLD_TYPES)
        {
            if (type != null)
            {
                dump.addData(
                        type.getName(),
                        Boolean.valueOf(type.isVersioned()).toString(),
                        String.valueOf(type.getVersion()),
                        String.valueOf(type.getId()));
            }
        }

        dump.addTitle("Name", "Versioned", "Version", "ID (GUI)");

        dump.setColumnAlignment(1, Alignment.RIGHT); // Versioned
        dump.setColumnProperties(2, Alignment.RIGHT, true); // Version
        dump.setColumnProperties(3, Alignment.RIGHT, true); // GUI ID

        return dump.getLines();
    }
}
