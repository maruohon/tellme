package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class SimpleForgeRegistryKeyOnlyDump
{
    public static List<String> getFormattedDump(Format format, IForgeRegistry<?> registry)
    {
        DataDump dump = new DataDump(1, format);

        for (ResourceLocation key : registry.getKeys())
        {
            dump.addData(key.toString());
        }

        dump.addTitle("Registry name");

        return dump.getLines();
    }
}
