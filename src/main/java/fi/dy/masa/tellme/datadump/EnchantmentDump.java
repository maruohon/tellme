package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;

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
                String name = ench.getDescriptionId() != null ? ench.getDescriptionId() : "<null>";
                String type = ench.category != null ? ench.category.toString() : "<null>";
                Enchantment.Rarity rarity = ench.getRarity();
                String rarityStr = rarity != null ? String.format("%s (%d)", rarity.toString(), rarity.getWeight()) : "<null>";
                @SuppressWarnings("deprecation")
                int intId = Registry.ENCHANTMENT.getId(ench);

                enchantmentDump.addData(regName, name, type, rarityStr, String.valueOf(intId));
            }
        }

        enchantmentDump.addTitle("Registry name", "Name", "Type", "Rarity", "ID");
        enchantmentDump.setColumnProperties(4, DataDump.Alignment.RIGHT, true);

        return enchantmentDump.getLines();
    }
}
