package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class StatTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Map.Entry<RegistryKey<StatType<?>>, StatType<?>> entry : ForgeRegistries.STAT_TYPES.getEntries())
        {
            StatType<?> type = entry.getValue();
            String typeName = type.getRegistryName().toString();

            for (Stat<?> stat : type)
            {
                dump.addData(typeName, stat.getName());
            }
        }

        dump.addTitle("Type registry name", "Stat name");

        return dump.getLines();
    }

    public static List<String> getFormattedDumpCustomStats(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (ResourceLocation key : Registry.CUSTOM_STAT.keySet())
        {
            String typeName = key.toString();
            Optional<ResourceLocation> stat = Registry.CUSTOM_STAT.func_241873_b(key);

            if (stat.isPresent())
            {
                dump.addData(typeName, stat.get().toString());
            }
        }

        dump.addTitle("Registry name", "Stat name");

        return dump.getLines();
    }
}
