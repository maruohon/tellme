package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import fi.dy.masa.tellme.mixin.IMixinWeightedPickerEntry;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class FeatureDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Identifier id : Registry.FEATURE.getIds())
        {
            Feature<?> type = Registry.FEATURE.get(id);
            String mobSpawns = getMobSpawnsString(type.getMonsterSpawns());
            String passiveSpawns = getMobSpawnsString(type.getCreatureSpawns());

            dump.addData(id.toString(), mobSpawns, passiveSpawns);
        }

        dump.addTitle("Registry name", "Mob spawns", "Passive spawns");

        return dump.getLines();
    }

    public static String getMobSpawnsString(Collection<Biome.SpawnEntry> list)
    {
        List<String> spawns = new ArrayList<>();

        for (Biome.SpawnEntry spawn : list)
        {
            Identifier erl = Registry.ENTITY_TYPE.getId(spawn.type);
            String entName = erl != null ? erl.toString() : "<null>";
            spawns.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, ((IMixinWeightedPickerEntry) spawn).getWeight(), spawn.minGroupSize, spawn.maxGroupSize));
        }

        Collections.sort(spawns);

        return String.join(", ", spawns);
    }
}
