package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.registries.ForgeRegistries;

import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class PaintingTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Map.Entry<ResourceKey<PaintingVariant>, PaintingVariant> entry : ForgeRegistries.PAINTING_VARIANTS.getEntries())
        {
            PaintingVariant type = entry.getValue();
            dump.addData(entry.getKey().location().toString(), String.valueOf(type.getWidth()), String.valueOf(type.getHeight()));
        }

        dump.addTitle("Registry name", "Width", "Height");

        return dump.getLines();
    }
}
