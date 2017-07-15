package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EntityDump extends DataDump
{
    private EntityDump(Format format)
    {
        super(4, format);
    }

    public static List<String> getFormattedEntityDump(Format format)
    {
        EntityDump entityDump = new EntityDump(format);
        Iterator<Map.Entry<ResourceLocation, EntityEntry>> iter = ForgeRegistries.ENTITIES.getEntries().iterator();

        while(iter.hasNext())
        {
            Map.Entry<ResourceLocation, EntityEntry> entry = iter.next();
            Class<? extends Entity> clazz = entry.getValue().getEntityClass();
            String className = clazz.getSimpleName();
            EntityRegistration er = EntityRegistry.instance().lookupModSpawn(clazz, true);

            if (er != null)
            {
                entityDump.addData(er.getContainer().getName(), er.getRegistryName().toString(), className, String.valueOf(er.getModEntityId()));
            }
            else
            {
                entityDump.addData("Minecraft", entry.getKey().toString(), className, String.valueOf(EntityList.getID(clazz)));
            }
        }

        entityDump.addTitle("Mod name", "Registry name", "Entity class name", "ID");

        entityDump.setColumnProperties(3, Alignment.RIGHT, true); // id

        entityDump.setUseColumnSeparator(true);

        return entityDump.getLines();
    }
}
