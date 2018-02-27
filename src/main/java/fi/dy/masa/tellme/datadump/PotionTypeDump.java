package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.List;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.mixin.IMixinPotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;

public class PotionTypeDump
{
    public static List<String> getFormattedPotionTypeDump(Format format)
    {
        DataDump potionTypeDump = new DataDump(3, format);

        for (ResourceLocation key : PotionType.REGISTRY.getKeys())
        {
            PotionType potionType = PotionType.REGISTRY.getObject(key);

            String regName = key.toString();
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
        String isBeneficial = String.valueOf(potion.isBeneficial());

        return "Potion:[reg:" + Potion.REGISTRY.getNameForObject(potion).toString() +
                    ",name:" + potion.getName() + ",isBad:" + isBad + ",isBeneficial:" + isBeneficial + "]";
    }

    public static String getPotionEffectInfo(PotionEffect effect)
    {
        return String.format("PotionEffect:{%s,amplifier:%d,duration:%d,isSplashPotion:%s,isAmbient:%s}",
                getPotionInfo(effect.getPotion()),
                effect.getAmplifier(),
                effect.getDuration(),
                ((IMixinPotionEffect) effect).getIsSplashPotion(),
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
}
