package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class ModListDump
{
    public static List<String> getFormattedModListDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(2, format);

        Map<String, ModContainer> mods = Loader.instance().getIndexedModList();

        for (Map.Entry<String, ModContainer> entry : mods.entrySet())
        {
            String modId = entry.getKey();
            String modName = entry.getValue().getName();

            dump.addData(modId, modName);
        }

        dump.addTitle("ModID", "Mod name");
        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }
}
