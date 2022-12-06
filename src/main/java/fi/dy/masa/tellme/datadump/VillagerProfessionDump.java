package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class VillagerProfessionDump
{
    public static List<String> getFormattedVillagerProfessionDump(DataDump.Format format)
    {
        DataDump villagerProfessionDump = new DataDump(2, format);

        for (Map.Entry<ResourceKey<VillagerProfession>, VillagerProfession> entry : ForgeRegistries.VILLAGER_PROFESSIONS.getEntries())
        {
            VillagerProfession profession = entry.getValue();
            String regName = entry.getKey().location().toString();

            villagerProfessionDump.addData(regName, profession.toString());
        }

        villagerProfessionDump.addTitle("Registry name", "Name");

        return villagerProfessionDump.getLines();
    }
}
