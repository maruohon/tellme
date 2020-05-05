package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class MemoryModuleTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(1, format);

        for (Map.Entry<ResourceLocation, MemoryModuleType<?>> entry : ForgeRegistries.MEMORY_MODULE_TYPES.getEntries())
        {
            MemoryModuleType<?> type = entry.getValue();
            dump.addData(type.getRegistryName().toString());
        }

        dump.addTitle("Registry name");

        return dump.getLines();
    }
}
