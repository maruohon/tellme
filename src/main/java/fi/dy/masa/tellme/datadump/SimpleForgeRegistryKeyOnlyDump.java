package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;
import net.minecraftforge.registries.IForgeRegistry;

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
