package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class VillagerProfessionDump
{
    private static final Field field_careers = ObfuscationReflectionHelper.findField(VillagerProfession.class, "careers"); // careers

    public static List<String> getFormattedVillagerProfessionDump(DataDump.Format format)
    {
        DataDump villagerProfessionDump = new DataDump(2, format);
        Iterator<Map.Entry<ResourceLocation, VillagerProfession>> iter = ForgeRegistries.VILLAGER_PROFESSIONS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, VillagerProfession> entry = iter.next();
            String regName = entry.getKey().toString();
            String careers = getCareersString(entry.getValue());

            villagerProfessionDump.addData(regName, careers);
        }

        villagerProfessionDump.addTitle("Registry name", "Careers");
        villagerProfessionDump.setUseColumnSeparator(true);

        return villagerProfessionDump.getLines();
    }

    private static String getCareersString(VillagerProfession profession)
    {
        List<VillagerCareer> careers = getCareers(profession);

        if (careers != null)
        {
            List<String> listCareerNames = new ArrayList<>();

            for (VillagerCareer career : careers)
            {
                listCareerNames.add(career.getName());
            }

            return String.join(", ", listCareerNames);
        }

        return "ERROR";
    }

    @Nullable
    public static List<VillagerCareer> getCareers(VillagerProfession profession)
    {
        try
        {
            @SuppressWarnings("unchecked")
            List<VillagerCareer> careers = (List<VillagerCareer>) field_careers.get(profession);
            return careers;
        }
        catch (Exception e)
        {
        }

        return null;
    }
}
