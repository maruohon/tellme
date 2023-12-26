package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.world.WorldType;

import malilib.util.StringUtils;

public class WorldTypeDump extends DataDump
{
    private WorldTypeDump(Format format)
    {
        super(5, format);
    }

    public static List<String> getFormattedWorldTypeDump(Format format)
    {
        WorldTypeDump dump = new WorldTypeDump(format);

        for (WorldType type : WorldType.WORLD_TYPES)
        {
            if (type != null)
            {
                dump.addData(
                        type.getName(),
                        StringUtils.translate("generator." + type.getName()),
                        Boolean.valueOf(type.isVersioned()).toString(),
                        String.valueOf(type.getVersion()),
                        String.valueOf(type.getId()));
            }
        }

        dump.addTitle("Level name", "Display name", "Versioned", "Version", "ID (GUI)");

        dump.setColumnAlignment(2, Alignment.RIGHT); // Versioned
        dump.setColumnProperties(3, Alignment.RIGHT, true); // Version
        dump.setColumnProperties(4, Alignment.RIGHT, true); // GUI ID

        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }
}
