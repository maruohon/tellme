package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ModListDump
{
    public static List<String> getFormattedModListDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (IModInfo modInfo : ModList.get().getMods())
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
