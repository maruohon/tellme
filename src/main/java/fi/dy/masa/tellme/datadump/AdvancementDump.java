package fi.dy.masa.tellme.datadump;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class AdvancementDump
{
    public static List<String> getFormattedAdvancementDumpSimple(Format format, @Nullable MinecraftServer server)
    {
        DataDump advancementDump = new DataDump(4, format);
        Iterable<Advancement> iterable = TellMe.dataProvider.getAdvacements(server);

        if (iterable == null)
        {
            return Collections.emptyList();
        }

        for (Advancement adv : iterable)
        {
            try
            {
                String id = adv.getId() != null ? adv.getId().toString() : "<null>";
                //String title = adv.getDisplay().getTitle().getUnformattedText();
                String title = adv.getChatComponent() != null ? adv.getChatComponent().getContents() : "<null>";
                DisplayInfo di = adv.getDisplay();
                String desc = di != null && di.getDescription() != null ? di.getDescription().getContents() : "<null>";
                String parent = adv.getParent() != null && adv.getParent().getId() != null ? adv.getParent().getId().toString() : "-";

                advancementDump.addData(id, title, desc, parent);
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Exception in AdvancementDump, for advancement '{}'", adv.getId(), e);
            }
        }

        advancementDump.addTitle("ID", "Name", "Description", "Parent");

        return advancementDump.getLines();
    }
}
