package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ActivityDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Map.Entry<ResourceKey<Activity>, Activity> entry : ForgeRegistries.ACTIVITIES.getEntries())
        {
            Activity type = entry.getValue();
            dump.addData(type.getRegistryName().toString(), type.getName());
        }

        dump.addTitle("Registry name", "Key");

        return dump.getLines();
    }
}
