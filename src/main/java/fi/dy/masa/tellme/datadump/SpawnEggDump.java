package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.mixin.IMixinSpawnEggItem;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SpawnEggDump
{
    public static List<String> getFormattedSpawnEggDump(DataDump.Format format)
    {
        DataDump spawnEggDump = new DataDump(4, format);

        for (SpawnEggItem egg : SpawnEggItem.getAll())
        {
            try
            {
                String id = Registry.ITEM.getId(egg).toString();
                String entityId = Registry.ENTITY_TYPE.getId(egg.getEntityType(null)).toString();
                int primaryColor = ((IMixinSpawnEggItem) egg).getPrimaryColor();
                int secondaryColor = ((IMixinSpawnEggItem) egg).getSecondaryColor();
                String colorPrimary = String.format("0x%08X (%10d)", primaryColor, primaryColor);
                String colorSecondary = String.format("0x%08X (%10d)", secondaryColor, secondaryColor);

                spawnEggDump.addData(id, entityId, colorPrimary, colorSecondary);
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
