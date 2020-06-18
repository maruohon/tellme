package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Optional;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class StatTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Identifier id : Registry.STAT_TYPE.getIds())
        {
            StatType<?> type = Registry.STAT_TYPE.get(id);
            String typeName = id.toString();

            for (Stat<?> stat : type)
            {
                dump.addData(typeName, stat.getName());
            }
        }

        dump.addTitle("Type registry name", "Stat name");

        return dump.getLines();
    }

    public static List<String> getFormattedDumpCustomStats(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Identifier key : Registry.CUSTOM_STAT.getIds())
        {
            String typeName = key.toString();
            Optional<Identifier> stat = Registry.CUSTOM_STAT.getOrEmpty(key);

            if (stat.isPresent())
            {
                dump.addData(typeName, stat.get().toString());
            }
        }

        dump.addTitle("Registry name", "Stat name");

        return dump.getLines();
    }
}
