package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class PaintingTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Identifier id : Registries.PAINTING_VARIANT.getIds())
        {
            PaintingVariant type = Registries.PAINTING_VARIANT.get(id);
            dump.addData(id.toString(), String.valueOf(type.getWidth()), String.valueOf(type.getHeight()));
        }

        dump.addTitle("Registry name", "Width", "Height");

        return dump.getLines();
    }
}
