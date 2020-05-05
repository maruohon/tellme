package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class StatTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Map.Entry<ResourceLocation, StatType<?>> entry : ForgeRegistries.STAT_TYPES.getEntries())
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
}
