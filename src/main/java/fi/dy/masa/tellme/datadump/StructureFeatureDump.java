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
    public static List<String> getFormattedDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Map.Entry<ResourceKey<StructureFeature<?>>, StructureFeature<?>> entry : ForgeRegistries.STRUCTURE_FEATURES.getEntries())
        {
            StructureFeature<?> feature = entry.getValue();
            ResourceLocation id = feature.getRegistryName();
            dump.addData(id.toString(), feature.getFeatureName());
        }

        dump.addTitle("Registry name", "Name");

        return dump.getLines();
    }

    public static String getMobSpawnsString(Collection<MobSpawnSettings.SpawnerData> list)
    {
        List<String> spawns = new ArrayList<>();

        for (MobSpawnSettings.SpawnerData spawn : list)
        {
            ResourceLocation erl = ForgeRegistries.ENTITIES.getKey(spawn.type);
            String entName = erl != null ? erl.toString() : "<null>";
            int weight = spawn.getWeight().asInt();
            spawns.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, weight, spawn.minCount, spawn.maxCount));
        }

        Collections.sort(spawns);

        return String.join(", ", spawns);
    }
}
