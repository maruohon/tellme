package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class StructureFeatureDump
{
    public static List<String> getFormattedDump(DataDump.Format format, boolean spawns)
    {
        DataDump dump = new DataDump(spawns ? 4 : 2, format);

        for (Identifier id : Registry.STRUCTURE_FEATURE.getIds())
        {
            StructureFeature<?> feature = Registry.STRUCTURE_FEATURE.get(id);

            if (spawns)
            {
                String mobSpawns = getMobSpawnsString(feature.getMonsterSpawns().getEntries());
                String passiveSpawns = getMobSpawnsString(feature.getCreatureSpawns().getEntries());
                dump.addData(id.toString(), feature.getName(), mobSpawns, passiveSpawns);
            }
            else
            {
                dump.addData(id.toString(), feature.getName());
            }
        }

        if (spawns)
        {
            dump.addTitle("Registry name", "Name", "Mob spawns", "Passive spawns");
        }
        else
        {
            dump.addTitle("Registry name", "Name");
        }

        return dump.getLines();
    }

    public static String getMobSpawnsString(Collection<SpawnSettings.SpawnEntry> list)
    {
        List<String> spawns = new ArrayList<>();

        for (SpawnSettings.SpawnEntry spawn : list)
        {
            Identifier erl = Registry.ENTITY_TYPE.getId(spawn.type);
            String entName = erl != null ? erl.toString() : "<null>";
            int weight = spawn.getWeight().getValue();
            spawns.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, weight, spawn.minGroupSize, spawn.maxGroupSize));
        }

        Collections.sort(spawns);

        return String.join(", ", spawns);
    }
}
