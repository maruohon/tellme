package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantmentDump
{
    public static List<String> getFormattedEnchantmentDump(DataDump.Format format)
    {
        DataDump enchantmentDump = new DataDump(5, format);
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
                @SuppressWarnings("deprecation")
                int id = Registry.ENCHANTMENT.getId(ench);

                enchantmentDump.addData(regName, name, type, rarity, String.valueOf(id));
            }
        }

        enchantmentDump.addTitle("Registry name", "Name", "Type", "Rarity", "ID");
        enchantmentDump.setColumnProperties(4, DataDump.Alignment.RIGHT, true);

        return enchantmentDump.getLines();
    }
}
