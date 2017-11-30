package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EnchantmentDump extends DataDump
{
    private EnchantmentDump(Format format)
    {
        super(5, format);
    }

    public static List<String> getFormattedEnchantmentDump(Format format)
    {
        EnchantmentDump enchantmentDump = new EnchantmentDump(format);
        Iterator<Map.Entry<ResourceLocation, Enchantment>> iter = ForgeRegistries.ENCHANTMENTS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Enchantment> entry = iter.next();
            Enchantment ench = entry.getValue();
            String regName = entry.getKey().toString();
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
