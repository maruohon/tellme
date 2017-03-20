package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

public class SpawnEggDump extends DataDump
{
    protected SpawnEggDump(Format format)
    {
        super(4, format);
    }

    public static List<String> getFormattedSpawnEggDump(Format format)
    {
        SpawnEggDump spawnEggDump = new SpawnEggDump(format);
        Iterator<Map.Entry<ResourceLocation, EntityList.EntityEggInfo>> iter = EntityList.ENTITY_EGGS.entrySet().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, EntityList.EntityEggInfo> entry = iter.next();
            EntityList.EntityEggInfo egg = entry.getValue();

            String colorPrimary = String.format("0x%08X (%10d)", egg.primaryColor, egg.primaryColor);
            String colorSecondary = String.format("0x%08X (%10d)", egg.secondaryColor, egg.secondaryColor);

            spawnEggDump.addData(entry.getKey().toString(), egg.spawnedID.toString(), colorPrimary, colorSecondary);
        }

        spawnEggDump.addTitle("Registry Name", "Spawned ID", "Egg primary color", "Egg secondary color");
        spawnEggDump.setUseColumnSeparator(true);

        return spawnEggDump.getLines();
    }
}
