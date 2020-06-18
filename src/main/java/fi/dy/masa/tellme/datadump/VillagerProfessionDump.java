package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class VillagerProfessionDump
{
    public static List<String> getFormattedVillagerProfessionDump(DataDump.Format format)
    {
        DataDump villagerProfessionDump = new DataDump(2, format);

        for (Identifier id : Registry.VILLAGER_PROFESSION.getIds())
        {
            String regName = id.toString();
            VillagerProfession profession = Registry.VILLAGER_PROFESSION.get(id);

            villagerProfessionDump.addData(regName, profession.toString());
        }

        villagerProfessionDump.addTitle("Registry name", "Name");

        return villagerProfessionDump.getLines();
    }
}
