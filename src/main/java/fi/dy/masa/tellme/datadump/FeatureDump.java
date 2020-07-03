package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class FeatureDump
{
    public static List<String> getFormattedDump(Format format, boolean spawns)
    {
        DataDump dump = new DataDump(spawns ? 3 : 1, format);

        for (Map.Entry<ResourceLocation, Feature<?>> entry : ForgeRegistries.FEATURES.getEntries())
        {
            Feature<?> feature = entry.getValue();

            if (spawns)
            {
                String mobSpawns = getMobSpawnsString(feature.getSpawnList());
                String passiveSpawns = getMobSpawnsString(feature.getCreatureSpawnList());
                dump.addData(entry.getKey().toString(), mobSpawns, passiveSpawns);
            }
            else
            {
                dump.addData(entry.getKey().toString());
            }
        }

        if (spawns)
        {
            dump.addTitle("Registry name", "Mob spawns", "Passive spawns");
        }
        else
        {
            dump.addTitle("Registry name");
        }

        return dump.getLines();
    }

    public static String getMobSpawnsString(Collection<SpawnListEntry> list)
    {
        List<String> spawns = new ArrayList<>();

        for (SpawnListEntry spawn : list)
        {
            ResourceLocation erl = spawn.entityType.getRegistryName();
            String entName = erl != null ? erl.toString() : "<null>";
            spawns.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, spawn.itemWeight, spawn.minGroupCount, spawn.maxGroupCount));
        }

        Collections.sort(spawns);

        return String.join(", ", spawns);
    }
}
