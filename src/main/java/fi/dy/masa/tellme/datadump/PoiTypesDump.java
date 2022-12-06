package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class PoiTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (Map.Entry<ResourceKey<PoiType>, PoiType> entry : ForgeRegistries.POI_TYPES.getEntries())
        {
            PoiType type = entry.getValue();
            dump.addData(entry.getKey().location().toString(), type.toString());
        }

        dump.addTitle("Registry name", "Name");

        return dump.getLines();
    }
}
