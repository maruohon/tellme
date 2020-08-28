package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class SoundEventDump
{
    public static List<String> getFormattedSoundEventDump(Format format)
    {
        DataDump soundEventDump = new DataDump(2, format);

        for (Map.Entry<RegistryKey<SoundEvent>, SoundEvent> entry : ForgeRegistries.SOUND_EVENTS.getEntries())
        {
            SoundEvent sound = entry.getValue();
            String regName = sound.getRegistryName().toString();

            @SuppressWarnings("deprecation")
            String id = String.valueOf(Registry.SOUND_EVENT.getId(sound));

            soundEventDump.addData(regName, id);
        }

        soundEventDump.addTitle("Registry name", "ID");

        soundEventDump.setColumnProperties(1, Alignment.RIGHT, true); // id

        return soundEventDump.getLines();
    }
}
