package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.ForgeRegistries;

import fi.dy.masa.tellme.util.RegistryUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class PotionDump
{
    public static List<String> getFormattedPotionDump(DataDump.Format format)
    {
        DataDump potionTypeDump = new DataDump(3, format);

        for (Map.Entry<ResourceKey<Potion>, Potion> entry : ForgeRegistries.POTIONS.getEntries())
        {
            Potion potion = entry.getValue();
            String regName = entry.getKey().location().toString();

            @SuppressWarnings("deprecation")
            String id = String.valueOf(Registry.POTION.getId(potion));

            List<MobEffectInstance> effects = potion.getEffects();

            potionTypeDump.addData(regName, id, String.join(", ", getEffectInfoLines(effects)));
        }

        potionTypeDump.addTitle("Registry name", "ID", "Effects");
        potionTypeDump.setColumnProperties(1, Alignment.RIGHT, true); // id

        return potionTypeDump.getLines();
    }

    public static String getMobEffectInfo(MobEffect effect)
    {
        String isBad = String.valueOf(effect.getCategory() == MobEffectCategory.HARMFUL);
        String isBeneficial = String.valueOf(effect.isBeneficial());

        return "MobEffect:[reg:" + RegistryUtils.getIdStr(effect, ForgeRegistries.MOB_EFFECTS) + ",name:" + effect.getDescriptionId() + ",isBad:" + isBad + ",isBeneficial:" + isBeneficial + "]";
    }

    public static String getMobEffectInstanceInfo(MobEffectInstance effect)
    {
        return String.format("MobEffectInstance:{%s,amplifier:%d,duration:%d,isAmbient:%s}",
                             getMobEffectInfo(effect.getEffect()),
                             effect.getAmplifier(),
                             effect.getDuration(),
                             effect.isAmbient());
    }

    public static List<String> getEffectInfoLines(List<MobEffectInstance> effects)
    {
        List<String> lines = new ArrayList<>();

        for (MobEffectInstance effect : effects)
        {
            lines.add(getMobEffectInstanceInfo(effect));
        }

        return lines;
    }
}
