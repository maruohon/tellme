package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.datadump.DataDump.Format;

public class SimpleVanillaRegistryKeyOnlyDump
{
    public static List<String> getFormattedDump(Format format, Registry<?> registry)
    {
        DataDump dump = new DataDump(1, format);

        for (ResourceLocation key : registry.keySet())
        {
            dump.addData(key.toString());
        }

        dump.addTitle("Registry name");

        return dump.getLines();
    }
}
