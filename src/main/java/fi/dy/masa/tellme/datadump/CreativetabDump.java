package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;

import fi.dy.masa.tellme.datadump.DataDump.Alignment;

public class CreativetabDump
{
    public static List<String> getFormattedCreativetabDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(4, format);

        for (int i = 0; i < CreativeTabs.CREATIVE_TAB_ARRAY.length; i++)
        {
            CreativeTabs tab = CreativeTabs.CREATIVE_TAB_ARRAY[i];

            if (tab != null)
            {
                String index = String.valueOf(i);
                String name = tab.getTabLabel();
                String translatedName = I18n.format(tab.getTranslationKey());
                String iconItem = ItemDump.getStackInfoBasic(tab.getIcon());

                dump.addData(index, name, translatedName, iconItem);
            }
        }

        dump.addTitle("Index", "Name", "Translated Name", "Icon Item");
        dump.setColumnProperties(0, Alignment.RIGHT, true); // index
        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }
}
