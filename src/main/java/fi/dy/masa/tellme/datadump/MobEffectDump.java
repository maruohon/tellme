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

public class MobEffectDump
{
    public static List<String> getFormattedPotionDump(DataDump.Format format)
    {
        DataDump potionDump = new DataDump(7, format);

        for (Map.Entry<ResourceKey<MobEffect>, MobEffect> entry : ForgeRegistries.MOB_EFFECTS.getEntries())
        {
            MobEffect effect = entry.getValue();
            ResourceLocation id = entry.getKey().location();

            @SuppressWarnings("deprecation")
            String numericId = String.valueOf(Registry.MOB_EFFECT.getId(effect));

            String modName = ModNameUtils.getModName(id);
            String regName = id.toString();
            String name = effect.getDescriptionId();
            String color = String.format("0x%08X (%10d)", effect.getColor(), effect.getColor());
            String isBad = String.valueOf(effect.getCategory() == MobEffectCategory.HARMFUL);
            String isBeneficial = String.valueOf(effect.isBeneficial());

            potionDump.addData(modName, regName, name, numericId, color, isBad, isBeneficial);
        }

        potionDump.addTitle("Mod name", "Registry name", "Effect Name", "ID", "Color", "Is Bad", "Is Beneficial");

        potionDump.setColumnProperties(3, Alignment.RIGHT, true); // id
        potionDump.setColumnAlignment(5, Alignment.RIGHT); // is bad
        potionDump.setColumnAlignment(6, Alignment.RIGHT); // is beneficial

        return potionDump.getLines();
    }
}
