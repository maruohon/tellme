package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SoundEventDump extends DataDump
{
    private SoundEventDump(Format format)
    {
        super(2, format);
    }

    public static List<String> getFormattedSoundEventDump(Format format)
    {
        SoundEventDump soundEventDump = new SoundEventDump(format);
        Iterator<Map.Entry<ResourceLocation, SoundEvent>> iter = ForgeRegistries.SOUND_EVENTS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, SoundEvent> entry = iter.next();
            String regName = entry.getKey().toString();
            String id = String.valueOf(SoundEvent.REGISTRY.getIDForObject(entry.getValue()));

            soundEventDump.addData(regName, id);
        }

        soundEventDump.addTitle("Registry name", "ID");
        soundEventDump.setColumnAlignment(1, Alignment.RIGHT); // id
        soundEventDump.setUseColumnSeparator(true);

        return soundEventDump.getLines();
    }
}
