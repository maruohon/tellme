package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;

public class SoundEventDump
{
    public static List<String> getFormattedSoundEventDump(Format format)
    {
        DataDump soundEventDump = new DataDump(2, format);

        for (Map.Entry<ResourceLocation, SoundEvent> entry : ForgeRegistries.SOUND_EVENTS.getEntries())
        {
            String regName = entry.getKey().toString();
            String id = String.valueOf(SoundEvent.REGISTRY.getIDForObject(entry.getValue()));

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

        TellMe.proxy.addMusicTypeData(musicTypeDump);

        musicTypeDump.addTitle("Name", "SoundEvent", "MinDelay", "MaxDelay");

        musicTypeDump.setColumnProperties(2, Alignment.RIGHT, true); // min delay
        musicTypeDump.setColumnProperties(3, Alignment.RIGHT, true); // max delay

        musicTypeDump.setUseColumnSeparator(true);

        return musicTypeDump.getLines();
    }
}
