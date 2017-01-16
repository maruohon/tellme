package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EntityList;

public class SpawnEggDump extends DataDump
{
    protected SpawnEggDump()
    {
        super(4);
    }

    public static List<String> getFormattedSpawnEggDump()
    {
        SpawnEggDump spawnEggDump = new SpawnEggDump();
        Iterator<Map.Entry<String, EntityList.EntityEggInfo>> iter = EntityList.ENTITY_EGGS.entrySet().iterator();

        while (iter.hasNext())
        {
            Map.Entry<String, EntityList.EntityEggInfo> entry = iter.next();
            EntityList.EntityEggInfo egg = entry.getValue();

            String colorPrimary = String.format("0x%08X (%10d)", egg.primaryColor, egg.primaryColor);
            String colorSecondary = String.format("0x%08X (%10d)", egg.secondaryColor, egg.secondaryColor);

            spawnEggDump.addData(entry.getKey(), egg.spawnedID, colorPrimary, colorSecondary);
        }

        spawnEggDump.addTitle("Name", "ID", "Egg primary color", "Egg secondary color");
        spawnEggDump.setUseColumnSeparator(true);

        return spawnEggDump.getLines();
    }
}
