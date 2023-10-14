package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class StatusEffectDump
{
    public static List<String> getFormattedPotionDump(DataDump.Format format)
    {
        DataDump potionDump = new DataDump(7, format);

        for (Identifier id : Registries.STATUS_EFFECT.getIds())
        {
            StatusEffect effect = Registries.STATUS_EFFECT.get(id);
            String modName = ModNameUtils.getModName(id);
            String regName = id.toString();
            String intId = String.valueOf(Registries.STATUS_EFFECT.getRawId(effect));
            String name = effect.getTranslationKey();
            String color = String.format("0x%08X (%10d)", effect.getColor(), effect.getColor());
            String isBad = String.valueOf(effect.getCategory() == StatusEffectCategory.HARMFUL);
            String isBeneficial = String.valueOf(effect.getCategory() == StatusEffectCategory.BENEFICIAL);

            potionDump.addData(modName, regName, name, intId, color, isBad, isBeneficial);
        }

        potionDump.addTitle("Mod name", "Registry name", "Potion Name", "ID", "Color", "Is bad", "Is beneficial");

        potionDump.setColumnProperties(3, Alignment.RIGHT, true); // id
        potionDump.setColumnAlignment(5, Alignment.RIGHT); // is bad
        potionDump.setColumnAlignment(6, Alignment.RIGHT); // is beneficial

        return potionDump.getLines();
    }
}
