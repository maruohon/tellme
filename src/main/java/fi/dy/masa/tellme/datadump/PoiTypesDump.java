package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class PoiTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Identifier id : Registries.POINT_OF_INTEREST_TYPE.getIds())
        {
            PointOfInterestType type = Registries.POINT_OF_INTEREST_TYPE.get(id);

            dump.addData(id.toString(), type.toString(), String.valueOf(type.searchDistance()));
        }

        dump.addTitle("Registry name", "Name", "Search distance");

        return dump.getLines();
    }
}
