package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.item.SpawnEggItem;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SpawnEggDump
{
    public static List<String> getFormattedSpawnEggDump(DataDump.Format format)
    {
        DataDump spawnEggDump = new DataDump(4, format);

        for (SpawnEggItem egg : SpawnEggItem.getEggs())
        {
            try
            {
                String id = egg.getRegistryName().toString();

                int primaryColor = egg.getColor(0);
                int secondaryColor = egg.getColor(1);
                String colorPrimary = String.format("0x%08X (%10d)", primaryColor, primaryColor);
                String colorSecondary = String.format("0x%08X (%10d)", secondaryColor, secondaryColor);

                spawnEggDump.addData(id, egg.getType(null).getRegistryName().toString(), colorPrimary, colorSecondary);
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Exception while dumping spawn eggs", e);
            }
        }

        spawnEggDump.addTitle("Registry Name", "Spawned ID", "Egg primary color", "Egg secondary color");

        return spawnEggDump.getLines();
    }
}
