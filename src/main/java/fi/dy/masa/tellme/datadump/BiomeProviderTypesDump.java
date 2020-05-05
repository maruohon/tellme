package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.provider.BiomeProviderType;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeProviderTypesDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(1, format);

        for (Map.Entry<ResourceLocation, BiomeProviderType<?, ?>> entry : ForgeRegistries.BIOME_PROVIDER_TYPES.getEntries())
        {
            BiomeProviderType<?, ?> type = entry.getValue();
            dump.addData(type.getRegistryName().toString());
        }

        dump.addTitle("Registry name");

        return dump.getLines();
    }
}
