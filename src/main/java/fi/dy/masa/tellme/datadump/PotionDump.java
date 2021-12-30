package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class PotionDump
{
    public static List<String> getFormattedPotionDump(DataDump.Format format)
    {
        DataDump potionDump = new DataDump(7, format);

        for (Map.Entry<ResourceKey<MobEffect>, MobEffect> entry : ForgeRegistries.POTIONS.getEntries())
        {
            MobEffect effect = entry.getValue();
            ResourceLocation rl = effect.getRegistryName();

            @SuppressWarnings("deprecation")
            String id = String.valueOf(Registry.MOB_EFFECT.getId(effect));

            String modName = ModNameUtils.getModName(rl);
            String regName = rl.toString();
            String name = effect.getDescriptionId();
            String color = String.format("0x%08X (%10d)", effect.getColor(), effect.getColor());
            String isBad = String.valueOf(effect.getCategory() == MobEffectCategory.HARMFUL);
            String isBeneficial = String.valueOf(effect.isBeneficial());

            potionDump.addData(modName, regName, name, id, color, isBad, isBeneficial);
        }

        potionDump.addTitle("Mod name", "Registry name", "Potion Name", "ID", "Liquid color", "Is bad", "Is beneficial");

        potionDump.setColumnProperties(3, Alignment.RIGHT, true); // id
        potionDump.setColumnAlignment(5, Alignment.RIGHT); // is bad
        potionDump.setColumnAlignment(6, Alignment.RIGHT); // is beneficial

        return potionDump.getLines();
    }
}
