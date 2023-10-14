package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class AdvancementDump
{
    public static List<String> getFormattedAdvancementDumpSimple(Format format, @Nullable MinecraftServer server)
    {
        DataDump advancementDump = new DataDump(4, format);
        Iterable<AdvancementEntry> iterable = server != null ? TellMe.dataProvider.getAdvancements(server) : null;

        if (iterable != null)
        {
            for (AdvancementEntry entry : iterable)
            {
                try
                {
                    String id = entry.id() != null ? entry.id().toString() : "<null>";
                    AdvancementDisplay di = entry.value().display().isPresent() ? entry.value().display().get() : null;
                    String title = di != null ? di.getTitle().getString() : "<null>";
                    String desc = di != null && di.getDescription() != null ? di.getDescription().getString() : "<null>";
                    String parent = entry.value().parent().isPresent() ? entry.value().parent().get().toString() : "-";

                    advancementDump.addData(id, title, desc, parent);
                }
                catch (Exception e)
                {
                    TellMe.logger.warn("Exception in AdvancementDump, for advancement '{}'", entry.id(), e);
                }
            }
        }

        advancementDump.addTitle("ID", "Name", "Description", "Parent");

        return advancementDump.getLines();
    }
}
