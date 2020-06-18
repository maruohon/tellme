package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ActivityDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Identifier id : Registry.ACTIVITY.getIds())
        {
            Activity type = Registry.ACTIVITY.get(id);
            dump.addData(id.toString(), type.getId());
        }

        dump.addTitle("Registry name", "Key");

        return dump.getLines();
    }
}
