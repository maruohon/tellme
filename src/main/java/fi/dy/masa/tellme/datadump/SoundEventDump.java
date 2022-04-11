package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;

public class SoundEventDump
{
    public static List<String> getFormattedSoundEventDump(Format format)
    {
        DataDump soundEventDump = new DataDump(2, format);

        for (ResourceLocation key : SoundEvent.REGISTRY.getKeys())
        {
            SoundEvent sound = SoundEvent.REGISTRY.getObject(key);
            String regName = key.toString();
            String id = String.valueOf(SoundEvent.REGISTRY.getIDForObject(sound));

            soundEventDump.addData(regName, id);
        }

        soundEventDump.addTitle("Registry name", "ID");

        soundEventDump.setColumnProperties(1, Alignment.RIGHT, true); // id

        soundEventDump.setUseColumnSeparator(true);

        return soundEventDump.getLines();
    }

    public static List<String> getFormattedMusicTypeDump(Format format)
    {
        DataDump musicTypeDump = new DataDump(4, format);

        addMusicTypeData(musicTypeDump);

        musicTypeDump.addTitle("Name", "SoundEvent", "MinDelay", "MaxDelay");

        musicTypeDump.setColumnProperties(2, Alignment.RIGHT, true); // min delay
        musicTypeDump.setColumnProperties(3, Alignment.RIGHT, true); // max delay

        musicTypeDump.setUseColumnSeparator(true);

        return musicTypeDump.getLines();
    }

    private static void addMusicTypeData(DataDump dump)
    {
        for (MusicType music : MusicType.values())
        {
            SoundEvent sound = music.getMusicLocation();
            String minDelay = String.valueOf(music.getMinDelay());
            String maxDelay = String.valueOf(music.getMaxDelay());
            ResourceLocation regName = SoundEvent.REGISTRY.getNameForObject(sound);

            dump.addData(music.name().toLowerCase(), regName != null ? regName.toString() : "<null>", minDelay, maxDelay);
        }
    }
}
