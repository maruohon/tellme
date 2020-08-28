package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class PotionDump
{
    public static List<String> getFormattedPotionDump(DataDump.Format format)
    {
        DataDump potionDump = new DataDump(7, format);

        for (Map.Entry<RegistryKey<Effect>, Effect> entry : ForgeRegistries.POTIONS.getEntries())
        {
            Effect effect = entry.getValue();
            ResourceLocation rl = effect.getRegistryName();

            @SuppressWarnings("deprecation")
            String id = String.valueOf(Registry.EFFECTS.getId(effect));

            String modName = ModNameUtils.getModName(rl);
            String regName = rl.toString();
            String name = effect.getName();
            String color = String.format("0x%08X (%10d)", effect.getLiquidColor(), effect.getLiquidColor());
            String isBad = String.valueOf(effect.getEffectType() == EffectType.HARMFUL);
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
