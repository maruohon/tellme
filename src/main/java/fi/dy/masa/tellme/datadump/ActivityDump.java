package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ActivityDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Map.Entry<RegistryKey<Activity>, Activity> entry : ForgeRegistries.ACTIVITIES.getEntries())
        {
            Activity type = entry.getValue();
            dump.addData(type.getRegistryName().toString(), type.getKey());
        }

        dump.addTitle("Registry name", "Key");

        return dump.getLines();
    }
}
