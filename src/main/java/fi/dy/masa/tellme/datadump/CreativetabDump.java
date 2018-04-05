package fi.dy.masa.tellme.datadump;

import java.util.List;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;

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
                String translatedName = I18n.format(tab.getTranslatedTabLabel());
                String iconItem = ItemDump.getStackInfoBasic(tab.getIconItemStack());

                dump.addData(index, name, translatedName, iconItem);
            }
        }

        dump.addTitle("Index", "Name", "Translated Name", "Icon Item");
        dump.setColumnProperties(0, Alignment.RIGHT, true); // index
        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }
}
