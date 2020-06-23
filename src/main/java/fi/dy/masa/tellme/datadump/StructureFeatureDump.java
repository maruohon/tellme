package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.structure.Structure;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class StructureFeatureDump
{
    public static List<String> getFormattedDump(DataDump.Format format, boolean spawns)
    {
        DataDump dump = new DataDump(spawns ? 5 : 3, format);

        for (ResourceLocation id : Registry.STRUCTURE_FEATURE.keySet())
        {
            Structure<?> feature = Registry.STRUCTURE_FEATURE.getOrDefault(id);

            if (spawns)
            {
                String mobSpawns = FeatureDump.getMobSpawnsString(feature.getSpawnList());
                String passiveSpawns = FeatureDump.getMobSpawnsString(feature.getCreatureSpawnList());
                dump.addData(id.toString(), feature.getStructureName(), String.valueOf(feature.getSize()), mobSpawns, passiveSpawns);
            }
            else
            {
                dump.addData(id.toString(), feature.getStructureName(), String.valueOf(feature.getSize()));
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
