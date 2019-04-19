package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;

public class PotionTypeDump extends DataDump
{
    private static final Field field_isSplashPotion = ObfuscationReflectionHelper.findField(PotionEffect.class, "field_82723_d"); // isSplashPotion

    protected PotionTypeDump(Format format)
    {
        super(3, format);
    }

    public static List<String> getFormattedPotionTypeDump(Format format)
    {
        PotionTypeDump potionTypeDump = new PotionTypeDump(format);
        Iterator<Map.Entry<ResourceLocation, PotionType>> iter = ForgeRegistries.POTION_TYPES.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, PotionType> entry = iter.next();
            PotionType potionType = entry.getValue();

            String regName = entry.getKey().toString();
            String id = String.valueOf(PotionType.REGISTRY.getIDForObject(potionType));
            List<PotionEffect> effects = potionType.getEffects();

            potionTypeDump.addData(regName, id, String.join(", ", getEffectInfoLines(effects)));
        }

        potionTypeDump.addTitle("Registry name", "ID", "Effects");

        potionTypeDump.setColumnProperties(1, Alignment.RIGHT, true); // id

        potionTypeDump.setUseColumnSeparator(true);

        return potionTypeDump.getLines();
    }

    public static String getPotionInfo(Potion potion)
    {
        String isBad = String.valueOf(potion.isBadEffect());
        String isBeneficial = PotionDump.getIsBeneficial(potion);

        return "Potion:[reg:" + potion.getRegistryName().toString() + ",name:" + potion.getName() + ",isBad:" + isBad + ",isBeneficial:" + isBeneficial + "]";
    }

    public static String getPotionEffectInfo(PotionEffect effect)
    {
        return String.format("PotionEffect:{%s,amplifier:%d,duration:%d,isSplashPotion:%s,isAmbient:%s}",
                getPotionInfo(effect.getPotion()),
                effect.getAmplifier(),
                effect.getDuration(),
                getIsSplashPotion(effect),
                effect.getIsAmbient());
    }

    public static List<String> getEffectInfoLines(List<PotionEffect> effects)
    {
        List<String> effectStrs = new ArrayList<String>();
        for (PotionEffect effect : effects)
        {
            effectStrs.add(getPotionEffectInfo(effect));
        }
        return effectStrs;
    }

    public static String getIsSplashPotion(PotionEffect effect)
    {
        try
        {
            return String.valueOf(field_isSplashPotion.get(effect));
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Failed to reflect PotionEffect#isSplashPotion", e);
            return "";
        }
    }
}
