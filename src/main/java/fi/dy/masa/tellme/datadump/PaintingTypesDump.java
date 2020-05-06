package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class PaintingTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Map.Entry<ResourceLocation, PaintingType> entry : ForgeRegistries.PAINTING_TYPES.getEntries())
        {
            PaintingType type = entry.getValue();
            dump.addData(type.getRegistryName().toString(), String.valueOf(type.getWidth()), String.valueOf(type.getHeight()));
        }

        dump.addTitle("Registry name", "Width", "Height");

        return dump.getLines();
    }
}
