package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.util.RegistryKey;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class PoiTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Map.Entry<RegistryKey<PointOfInterestType>, PointOfInterestType> entry : ForgeRegistries.POI_TYPES.getEntries())
        {
            PointOfInterestType type = entry.getValue();
            dump.addData(type.getRegistryName().toString(), type.toString());
        }

        dump.addTitle("Registry name", "Name");

        return dump.getLines();
    }
}
