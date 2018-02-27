package fi.dy.masa.tellme.datadump;

import java.util.List;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;

public class EnchantmentDump
{
    public static List<String> getFormattedEnchantmentDump(Format format)
    {
        DataDump enchantmentDump = new DataDump(5, format);

        for (ResourceLocation key : Enchantment.REGISTRY.getKeys())
        {
            Enchantment ench = Enchantment.REGISTRY.getObject(key);
            String regName = key.toString();
            String name = ench.getName();
            String type = ench.type.toString();
            String rarity = ench.getRarity().toString();
            int id = Enchantment.getEnchantmentID(ench);

            enchantmentDump.addData(regName, name, type, rarity, String.valueOf(id));
        }

        enchantmentDump.addTitle("Registry name", "Name", "Type", "Rarity", "ID");
        enchantmentDump.setColumnProperties(4, Alignment.RIGHT, true);
        enchantmentDump.setUseColumnSeparator(true);

        return enchantmentDump.getLines();
    }
}
