package fi.dy.masa.tellme.datadump;

import java.util.List;
import fi.dy.masa.tellme.util.datadump.DataDump;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class ModListDump
{
    public static List<String> getFormattedModListDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(2, format);

        for (ModInfo modInfo : ModList.get().getMods())
        {
            String modId = modInfo.getModId();
            String modName = modInfo.getDisplayName();

            dump.addData(modId, modName);
        }

        dump.addTitle("Mod ID", "Mod name");

        return dump.getLines();
    }
}
