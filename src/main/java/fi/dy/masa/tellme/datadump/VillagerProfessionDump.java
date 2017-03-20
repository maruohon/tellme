package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class VillagerProfessionDump extends DataDump
{
    private static final Field field_careers = ReflectionHelper.findField(VillagerProfession.class, "careers");

    private VillagerProfessionDump(Format format)
    {
        super (2, format);
    }

    public static List<String> getFormattedVillagerProfessionDump(Format format)
    {
        VillagerProfessionDump villagerProfessionDump = new VillagerProfessionDump(format);
        Iterator<Map.Entry<ResourceLocation, VillagerProfession>> iter = ForgeRegistries.VILLAGER_PROFESSIONS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, VillagerProfession> entry = iter.next();
            String regName = entry.getKey().toString();
            String careers = getCareers(entry.getValue());

            villagerProfessionDump.addData(regName, careers);
        }

        villagerProfessionDump.addTitle("Registry name", "Careers");
        villagerProfessionDump.setUseColumnSeparator(true);

        return villagerProfessionDump.getLines();
    }

    private static String getCareers(VillagerProfession profession)
    {
        try
        {
            List<String> listCareers = new ArrayList<String>();
            @SuppressWarnings("unchecked")
            List<VillagerCareer> careers = (List<VillagerCareer>) field_careers.get(profession);

            for (VillagerCareer career : careers)
            {
                listCareers.add(career.getName());
            }

            return String.join(", ", listCareers);
        }
        catch (Exception e)
        {
            return "ERROR";
        }
    }
}
