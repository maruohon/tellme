package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.PointOfInterestType;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class PoiTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Identifier id : Registry.POINT_OF_INTEREST_TYPE.getIds())
        {
            PointOfInterestType type = Registry.POINT_OF_INTEREST_TYPE.get(id);
            @Nullable SoundEvent sound = type.getSound();
            String workSound = sound != null ? Registry.SOUND_EVENT.getId(sound).toString() : "-";

            dump.addData(id.toString(), type.toString(), workSound);
        }

        dump.addTitle("Registry name", "Name", "Work sound");

        return dump.getLines();
    }
}
