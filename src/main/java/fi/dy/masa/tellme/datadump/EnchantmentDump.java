package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class EnchantmentDump
{
    public static List<String> getFormattedEnchantmentDump(DataDump.Format format)
    {
        DataDump enchantmentDump = new DataDump(5, format);

        for (Identifier key : Registry.ENCHANTMENT.getIds())
        {
            Enchantment ench = Registry.ENCHANTMENT.get(key);

            if (ench != null)
            {
                String regName = key.toString();
                String name = ench.getTranslationKey() != null ? ench.getTranslationKey() : "<null>";
                String type = ench.type != null ? ench.type.toString() : "<null>";
                String rarity = ench.getWeight() != null ? ench.getWeight().toString() : "<null>";
                int intId = Registry.ENCHANTMENT.getRawId(ench);

                enchantmentDump.addData(regName, name, type, rarity, String.valueOf(intId));
            }
        }

        enchantmentDump.addTitle("Registry name", "Name", "Type", "Rarity", "ID");
        enchantmentDump.setColumnProperties(4, DataDump.Alignment.RIGHT, true); // id

        return enchantmentDump.getLines();
    }
}
