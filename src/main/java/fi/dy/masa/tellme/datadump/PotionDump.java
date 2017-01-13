package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.ModNameUtils;

public class PotionDump extends DataDump
{
    private static final Field field_isBeneficial = ReflectionHelper.findField(Potion.class, "field_188415_h", "beneficial");

    private PotionDump()
    {
        super(7);
    }

    public static List<String> getFormattedPotionDump()
    {
        PotionDump potionDump = new PotionDump();
        Iterator<Map.Entry<ResourceLocation, Potion>> iter = ForgeRegistries.POTIONS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Potion> entry = iter.next();
            Potion potion = entry.getValue();
            ResourceLocation rl = entry.getKey();
            String modName = ModNameUtils.getModName(rl);
            String regName = rl.toString();
            String id = String.valueOf(Potion.getIdFromPotion(potion));
            String name = potion.getName();
            String color = String.format("0x%08X (%10d)", potion.getLiquidColor(), potion.getLiquidColor());
            String isBad = String.valueOf(potion.isBadEffect());
            String isBeneficial = getIsBeneficial(potion);

            potionDump.addData(modName, regName, name, id, color, isBad, isBeneficial);
        }

        potionDump.addTitle("Mod name", "Registry name", "Potion Name", "ID", "Liquid color", "Is bad", "Is beneficial");
        potionDump.setColumnAlignment(3, Alignment.RIGHT); // id
        potionDump.setColumnAlignment(5, Alignment.RIGHT); // is bad
        potionDump.setColumnAlignment(6, Alignment.RIGHT); // is beneficial
        potionDump.setUseColumnSeparator(true);

        return potionDump.getLines();
    }

    private static String getIsBeneficial(Potion potion)
    {
        try
        {
            return String.valueOf(field_isBeneficial.get(potion));
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Failed to reflect Potion#beneficial", e);
            return "";
        }
    }
}
