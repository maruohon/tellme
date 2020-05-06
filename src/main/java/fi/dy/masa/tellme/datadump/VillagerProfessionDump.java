package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.util.datadump.DataDump;
import net.minecraftforge.registries.ForgeRegistries;

public class VillagerProfessionDump
{
    public static List<String> getFormattedVillagerProfessionDump(DataDump.Format format)
    {
        DataDump villagerProfessionDump = new DataDump(2, format);

        for (Map.Entry<ResourceLocation, VillagerProfession> entry : ForgeRegistries.PROFESSIONS.getEntries())
        {
            String regName = entry.getKey().toString();
            VillagerProfession profession = entry.getValue();

            villagerProfessionDump.addData(regName, profession.toString());
        }

        villagerProfessionDump.addTitle("Registry name", "Name");

        return villagerProfessionDump.getLines();
    }
}
