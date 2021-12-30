package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class WorldPresetDump
{
    public static List<String> getFormattedDump(DataDump.Format format)
    {
        DataDump potionDump = new DataDump(2, format);

        for (Map.Entry<ResourceKey<ForgeWorldPreset>, ForgeWorldPreset> entry : ForgeRegistries.WORLD_TYPES.getEntries())
        {
            ForgeWorldPreset type = entry.getValue();
            ResourceLocation id = type.getRegistryName();

            potionDump.addData(id.toString(), type.getDisplayName().getString());
        }

        potionDump.addTitle("Registry name", "Display Name");

        return potionDump.getLines();
    }
}
