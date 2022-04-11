package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ModNameUtils;

public class PotionDump
{
    public static List<String> getFormattedPotionDump(Format format)
    {
        DataDump potionDump = new DataDump(7, format);

        for (ResourceLocation key : Potion.REGISTRY.getKeys())
        {
            Potion potion = Potion.REGISTRY.getObject(key);
            String modName = ModNameUtils.getModName(key);
            String regName = key.toString();
            String id = String.valueOf(Potion.getIdFromPotion(potion));
            String name = potion.getName();
            String color = String.format("0x%08X (%10d)", potion.getLiquidColor(), potion.getLiquidColor());
            String isBad = String.valueOf(potion.isBadEffect());
            String isBeneficial = String.valueOf(potion.isBeneficial());

            potionDump.addData(modName, regName, name, id, color, isBad, isBeneficial);
        }

        potionDump.addTitle("Mod name", "Registry name", "Potion Name", "ID", "Liquid color", "Is bad", "Is beneficial");

        potionDump.setColumnProperties(3, Alignment.RIGHT, true); // id
        potionDump.setColumnAlignment(5, Alignment.RIGHT); // is bad
        potionDump.setColumnAlignment(6, Alignment.RIGHT); // is beneficial

        potionDump.setUseColumnSeparator(true);

        return potionDump.getLines();
    }
}
