package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(1, format);

        for (Map.Entry<ResourceLocation, ContainerType<?>> entry : ForgeRegistries.CONTAINERS.getEntries())
        {
            ContainerType<?> type = entry.getValue();
            dump.addData(type.getRegistryName().toString());
        }

        dump.addTitle("Registry name");

        return dump.getLines();
    }
}
