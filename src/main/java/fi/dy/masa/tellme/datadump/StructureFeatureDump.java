package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class StructureFeatureDump
{
    public static List<String> getFormattedDump(DataDump.Format format, boolean spawns)
    {
        DataDump dump = new DataDump(spawns ? 5 : 3, format);

        for (Identifier id : Registry.STRUCTURE_FEATURE.getIds())
        {
            StructureFeature<?> feature = Registry.STRUCTURE_FEATURE.get(id);

            if (spawns)
            {
                String mobSpawns = FeatureDump.getMobSpawnsString(feature.getMonsterSpawns());
                String passiveSpawns = FeatureDump.getMobSpawnsString(feature.getCreatureSpawns());
                dump.addData(id.toString(), feature.getName(), String.valueOf(feature.getRadius()), mobSpawns, passiveSpawns);
            }
            else
            {
                dump.addData(id.toString(), feature.getName(), String.valueOf(feature.getRadius()));
            }
        }

        if (spawns)
        {
            dump.addTitle("Registry name", "Name", "Radius", "Mob spawns", "Passive spawns");
        }
        else
        {
            dump.addTitle("Registry name", "Name", "Radius");
        }

        return dump.getLines();
    }
}
