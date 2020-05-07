package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.PointOfInterestType;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class PoiTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Map.Entry<ResourceLocation, PointOfInterestType> entry : ForgeRegistries.POI_TYPES.getEntries())
        {
            dump.addData(entry.getKey().toString(), entry.getValue().toString());
        }

        dump.addTitle("Registry name", "Name");

        return dump.getLines();
    }
}
