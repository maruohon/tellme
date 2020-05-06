package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.feature.Feature;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class FeatureDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Map.Entry<ResourceLocation, Feature<?>> entry : ForgeRegistries.FEATURES.getEntries())
        {
            Feature<?> type = entry.getValue();
            String mobSpawns = getMobSpawnsString(type.getSpawnList());
            String passiveSpawns = getMobSpawnsString(type.getCreatureSpawnList());

            dump.addData(type.getRegistryName().toString(), mobSpawns, passiveSpawns);
        }

        dump.addTitle("Registry name", "Mob spawns", "Passive spawns");

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
