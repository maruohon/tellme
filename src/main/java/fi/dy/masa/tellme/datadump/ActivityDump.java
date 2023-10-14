package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ActivityDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Identifier id : Registries.ACTIVITY.getIds())
        {
            Activity type = Registries.ACTIVITY.get(id);
            dump.addData(id.toString(), type.getId());
        }

        dump.addTitle("Registry name", "Key");

        return dump.getLines();
    }
}
