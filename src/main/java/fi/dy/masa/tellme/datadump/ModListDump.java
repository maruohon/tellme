package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.util.Formatting;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ModListDump
{
    public static List<String> getFormattedModListDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (net.fabricmc.loader.api.ModContainer container : net.fabricmc.loader.api.FabricLoader.getInstance().getAllMods())
        {
            String modId = container.getMetadata().getId();
            String modName = Formatting.strip(container.getMetadata().getName());
            String version = container.getMetadata().getVersion().getFriendlyString();

            dump.addData(modId, modName, version);
        }

        dump.addTitle("Mod ID", "Mod name", "Mod version");

        return dump.getLines();
    }
}
