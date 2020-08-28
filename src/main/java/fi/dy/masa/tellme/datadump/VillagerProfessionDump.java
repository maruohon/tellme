package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class VillagerProfessionDump
{
    public static List<String> getFormattedVillagerProfessionDump(DataDump.Format format)
    {
        DataDump villagerProfessionDump = new DataDump(2, format);

        for (Map.Entry<RegistryKey<VillagerProfession>, VillagerProfession> entry : ForgeRegistries.PROFESSIONS.getEntries())
        {
            VillagerProfession profession = entry.getValue();
            String regName = profession.getRegistryName().toString();

            villagerProfessionDump.addData(regName, profession.toString());
        }

        villagerProfessionDump.addTitle("Registry name", "Name");

        return villagerProfessionDump.getLines();
    }
}
