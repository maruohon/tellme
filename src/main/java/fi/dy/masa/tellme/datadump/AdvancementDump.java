package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class AdvancementDump
{
    public static List<String> getFormattedAdvancementDumpSimple(Format format, @Nullable MinecraftServer server)
    {
        DataDump advancementDump = new DataDump(4, format);
        Iterable<Advancement> iterable = server != null ? TellMe.dataProvider.getAdvancements(server) : null;

        if (iterable != null)
        {
            for (Advancement adv : iterable)
            {
                try
                {
                    String id = adv.getId() != null ? adv.getId().toString() : "<null>";
                    String title = adv.toHoverableText() != null ? adv.toHoverableText().getString() : "<null>";
                    AdvancementDisplay di = adv.getDisplay();
                    String desc = di != null && di.getDescription() != null ? di.getDescription().getString() : "<null>";
                    String parent = adv.getParent() != null && adv.getParent().getId() != null ? adv.getParent().getId().toString() : "-";

                    advancementDump.addData(id, title, desc, parent);
                }
                catch (Exception e)
                {
                    TellMe.logger.warn("Exception in AdvancementDump, for advancement '{}'", adv.getId(), e);
                }
            }
        }

        advancementDump.addTitle("ID", "Name", "Description", "Parent");

        return advancementDump.getLines();
    }
}
