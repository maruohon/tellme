package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.village.PointOfInterestType;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class PoiTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Map.Entry<ResourceLocation, PointOfInterestType> entry : ForgeRegistries.POI_TYPES.getEntries())
        {
            PointOfInterestType type = entry.getValue();
            @Nullable SoundEvent sound = type.getWorkSound();
            String workSound = sound != null ? sound.getRegistryName().toString() : "-";

            dump.addData(type.getRegistryName().toString(), type.toString(), workSound);
        }

        dump.addTitle("Registry name", "Name", "Work sound");

        return dump.getLines();
    }
}
