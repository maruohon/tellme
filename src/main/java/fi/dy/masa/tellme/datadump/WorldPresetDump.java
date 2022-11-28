package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class WorldPresetDump
{
    @SuppressWarnings("unchecked")
    public static List<String> getFormattedDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(1, format);

        Field fieldPresets;
        List<WorldPreset> list = null;

        try
        {
            fieldPresets = ObfuscationReflectionHelper.findField(WorldPreset.class, "f_101508_");
            list = (List<WorldPreset>) fieldPresets.get(null);
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Exception while reflecting World Presets list fields", e);
        }

        if (list != null)
        {
            for (WorldPreset preset : list)
            {
                dump.addData(preset.description().getString());
            }
        }

        dump.addTitle("Name");

        return dump.getLines();
    }
}
