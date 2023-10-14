package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import fi.dy.masa.tellme.util.datadump.DataDump;

public class EnchantmentDump
{
    public static List<String> getFormattedEnchantmentDump(DataDump.Format format)
    {
        DataDump enchantmentDump = new DataDump(5, format);

        for (Identifier key : Registries.ENCHANTMENT.getIds())
        {
            Enchantment ench = Registries.ENCHANTMENT.get(key);

            if (ench != null)
            {
                String regName = key.toString();
                String name = ench.getTranslationKey() != null ? ench.getTranslationKey() : "<null>";
                String target = ench.target != null ? ench.target.toString() : "<null>";
                Enchantment.Rarity rarity = ench.getRarity();
                String rarityStr = rarity != null ? String.format("%s (%d)", rarity, rarity.getWeight()) : "<null>";
                int intId = Registries.ENCHANTMENT.getRawId(ench);

                enchantmentDump.addData(regName, name, target, rarityStr, String.valueOf(intId));
            }
        }

        enchantmentDump.addTitle("Registry name", "Name", "Target", "Rarity", "ID");
        enchantmentDump.setColumnProperties(4, DataDump.Alignment.RIGHT, true); // id

        return enchantmentDump.getLines();
    }
}
