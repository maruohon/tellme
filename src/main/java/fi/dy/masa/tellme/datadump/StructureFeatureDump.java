package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class StructureFeatureDump
{
    public static List<String> getFormattedDump(DataDump.Format format, boolean spawns)
    {
        DataDump dump = new DataDump(spawns ? 4 : 2, format);

        for (Map.Entry<RegistryKey<Structure<?>>, Structure<?>> entry : ForgeRegistries.STRUCTURE_FEATURES.getEntries())
        {
            Structure<?> feature = entry.getValue();
            ResourceLocation id = feature.getRegistryName();

            if (spawns)
            {
                String mobSpawns = getMobSpawnsString(feature.getSpawnList());
                String passiveSpawns = getMobSpawnsString(feature.getCreatureSpawnList());
                dump.addData(id.toString(), feature.getStructureName(), mobSpawns, passiveSpawns);
            }
            else
            {
                dump.addData(id.toString(), feature.getStructureName());
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

    public static String getMobSpawnsString(Collection<MobSpawnInfo.Spawners> list)
    {
        List<String> spawns = new ArrayList<>();

        for (MobSpawnInfo.Spawners spawn : list)
        {
            ResourceLocation erl = ForgeRegistries.ENTITIES.getKey(spawn.field_242588_c);
            String entName = erl != null ? erl.toString() : "<null>";
            spawns.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, spawn.itemWeight, spawn.field_242589_d, spawn.field_242590_e));
        }

        Collections.sort(spawns);

        return String.join(", ", spawns);
    }
}
