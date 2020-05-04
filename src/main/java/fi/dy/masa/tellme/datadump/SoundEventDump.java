package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class SoundEventDump
{
    public static List<String> getFormattedSoundEventDump(Format format)
    {
        DataDump soundEventDump = new DataDump(2, format);

        for (Map.Entry<ResourceLocation, SoundEvent> entry : ForgeRegistries.SOUND_EVENTS.getEntries())
        {
            String regName = entry.getKey().toString();

            @SuppressWarnings("deprecation")
            String id = String.valueOf(Registry.SOUND_EVENT.getId(entry.getValue()));

            soundEventDump.addData(regName, id);
        }

        soundEventDump.addTitle("Registry name", "ID");

        soundEventDump.setColumnProperties(1, Alignment.RIGHT, true); // id

        return soundEventDump.getLines();
    }

    public static List<String> getFormattedMusicTypeDump(Format format)
    {
        DataDump musicTypeDump = new DataDump(4, format);

        TellMe.dataProvider.addMusicTypeData(musicTypeDump);

        musicTypeDump.addTitle("Name", "SoundEvent", "MinDelay", "MaxDelay");

        musicTypeDump.setColumnProperties(2, Alignment.RIGHT, true); // min delay
        musicTypeDump.setColumnProperties(3, Alignment.RIGHT, true); // max delay

        return musicTypeDump.getLines();
    }
}
