package fi.dy.masa.tellme.datadump;

import java.util.List;
import fi.dy.masa.tellme.util.datadump.DataDump;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class ModListDump
{
    public static List<String> getFormattedModListDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (ModInfo modInfo : ModList.get().getMods())
        {
            String modId = modInfo.getModId();
            String modName = modInfo.getDisplayName();
            String modVersion = modInfo.getVersion().toString();

            dump.addData(modId, modName, modVersion);
        }

        dump.addTitle("Mod ID", "Mod name", "Mod version");

        return dump.getLines();
    }
}
