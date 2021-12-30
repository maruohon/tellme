package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class StructureFeatureDump
{
    public static List<String> getFormattedDump(DataDump.Format format, boolean spawns)
    {
        DataDump dump = new DataDump(spawns ? 4 : 2, format);

        for (Map.Entry<ResourceKey<StructureFeature<?>>, StructureFeature<?>> entry : ForgeRegistries.STRUCTURE_FEATURES.getEntries())
        {
            StructureFeature<?> feature = entry.getValue();
            ResourceLocation id = feature.getRegistryName();

            if (spawns)
            {
                String mobSpawns = getMobSpawnsString(feature.getSpecialEnemies());
                String passiveSpawns = getMobSpawnsString(feature.getSpecialAnimals());
                dump.addData(id.toString(), feature.getFeatureName(), mobSpawns, passiveSpawns);
            }
            else
            {
                dump.addData(id.toString(), feature.getFeatureName());
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

    public static String getMobSpawnsString(Collection<MobSpawnSettings.SpawnerData> list)
    {
        List<String> spawns = new ArrayList<>();

        for (MobSpawnSettings.SpawnerData spawn : list)
        {
            ResourceLocation erl = ForgeRegistries.ENTITIES.getKey(spawn.type);
            String entName = erl != null ? erl.toString() : "<null>";
            spawns.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, spawn.weight, spawn.minCount, spawn.maxCount));
        }

        Collections.sort(spawns);

        return String.join(", ", spawns);
    }
}
