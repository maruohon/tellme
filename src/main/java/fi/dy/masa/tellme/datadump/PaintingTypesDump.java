package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class PaintingTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Identifier id : Registry.MOTIVE.getIds())
        {
            PaintingMotive type = Registry.MOTIVE.get(id);
            dump.addData(id.toString(), String.valueOf(type.getWidth()), String.valueOf(type.getHeight()));
        }

        dump.addTitle("Registry name", "Width", "Height");

        return dump.getLines();
    }
}
