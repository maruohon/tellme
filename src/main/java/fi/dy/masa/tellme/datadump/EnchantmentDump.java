package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EnchantmentDump extends DataDump
{
    private EnchantmentDump()
    {
        super(4);
    }

    public static List<String> getFormattedEnchantmentDump()
    {
        EnchantmentDump enchantmentDump = new EnchantmentDump();
        Iterator<Map.Entry<ResourceLocation, Enchantment>> iter = ForgeRegistries.ENCHANTMENTS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Enchantment> entry = iter.next();
            Enchantment ench = entry.getValue();
            String regName = entry.getKey().toString();
            String name = ench.getName();
            String type = ench.type.toString();
            String rarity = ench.getRarity().toString();

            enchantmentDump.addData(regName, name, type, rarity);
        }

        enchantmentDump.addTitle("Registry name", "Name", "Type", "Rarity");
        enchantmentDump.setUseColumnSeparator(true);

        return enchantmentDump.getLines();
    }
}
