package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class SoundEventDump
{
    public static List<String> getFormattedSoundEventDump(Format format)
    {
        DataDump soundEventDump = new DataDump(2, format);

        for (Identifier id : Registry.SOUND_EVENT.getIds())
        {
            SoundEvent sound = Registry.SOUND_EVENT.get(id);
            String intId = String.valueOf(Registry.SOUND_EVENT.getRawId(sound));

            soundEventDump.addData(id.toString(), intId);
        }

        soundEventDump.addTitle("Registry name", "ID");

        soundEventDump.setColumnProperties(1, Alignment.RIGHT, true); // id

        return soundEventDump.getLines();
    }
}
