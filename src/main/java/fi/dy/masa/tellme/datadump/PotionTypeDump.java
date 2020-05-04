package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import net.minecraftforge.registries.ForgeRegistries;

public class PotionTypeDump
{
    public static List<String> getFormattedPotionTypeDump(DataDump.Format format)
    {
        DataDump potionTypeDump = new DataDump(3, format);

        for (Map.Entry<ResourceLocation, Potion> entry : ForgeRegistries.POTION_TYPES.getEntries())
        {
            String regName = entry.getKey().toString();
            Potion potion = entry.getValue();

            @SuppressWarnings("deprecation")
            String id = String.valueOf(Registry.POTION.getId(potion));

            List<EffectInstance> effects = potion.getEffects();

            potionTypeDump.addData(regName, id, String.join(", ", getEffectInfoLines(effects)));
        }

        potionTypeDump.addTitle("Registry name", "ID", "Effects");
        potionTypeDump.setColumnProperties(1, Alignment.RIGHT, true); // id

        return potionTypeDump.getLines();
    }

    public static String getEffectInfo(Effect effect)
    {
        String isBad = String.valueOf(effect.getEffectType() == EffectType.HARMFUL);
        String isBeneficial = String.valueOf(effect.isBeneficial());

        return "Potion:[reg:" + effect.getRegistryName().toString() + ",name:" + effect.getName() + ",isBad:" + isBad + ",isBeneficial:" + isBeneficial + "]";
    }

    public static String getPotionEffectInfo(EffectInstance effect)
    {
        return String.format("PotionEffect:{%s,amplifier:%d,duration:%d,isAmbient:%s}",
                getEffectInfo(effect.getPotion()),
                effect.getAmplifier(),
                effect.getDuration(),
                effect.isAmbient());
    }

    public static List<String> getEffectInfoLines(List<EffectInstance> effects)
    {
        List<String> effectStrs = new ArrayList<String>();

        for (EffectInstance effect : effects)
        {
            effectStrs.add(getPotionEffectInfo(effect));
        }

        return effectStrs;
    }
}
