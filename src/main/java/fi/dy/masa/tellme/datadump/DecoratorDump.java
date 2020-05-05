package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.placement.Placement;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class DecoratorDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(1, format);

        for (Map.Entry<ResourceLocation, Placement<?>> entry : ForgeRegistries.DECORATORS.getEntries())
        {
            Placement<?> type = entry.getValue();
            dump.addData(type.getRegistryName().toString());
        }

        dump.addTitle("Registry name");

        return dump.getLines();
    }
}
