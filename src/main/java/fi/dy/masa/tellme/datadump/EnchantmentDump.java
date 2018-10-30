package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Set;
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
        Set<ResourceLocation> keys = ForgeRegistries.ENCHANTMENTS.getKeys();

        for (ResourceLocation key : keys)
        {
            Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(key);

            if (ench != null)
            {
                String regName = key.toString();
                String name = ench.getName() != null ? ench.getName() : "<null>";
                String type = ench.type != null ? ench.type.toString() : "<null>";
                String rarity = ench.getRarity() != null ? ench.getRarity().toString() : "<null>";
                int id = Enchantment.getEnchantmentID(ench);

                enchantmentDump.addData(regName, name, type, rarity, String.valueOf(id));
            }
        }

        enchantmentDump.addTitle("Registry name", "Name", "Type", "Rarity", "ID");
        enchantmentDump.setColumnProperties(4, Alignment.RIGHT, true);
        enchantmentDump.setUseColumnSeparator(true);

        return enchantmentDump.getLines();
    }
}
