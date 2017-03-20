package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;

public class EntityDump extends DataDump
{
    private EntityDump(Format format)
    {
        super(5, format);
    }

    public static List<String> getFormattedEntityDump(Format format)
    {
        EntityDump entityDump = new EntityDump(format);
        Iterator<Map.Entry<String, Class<? extends Entity>>> iter = EntityList.NAME_TO_CLASS.entrySet().iterator();

        while (iter.hasNext())
        {
            Map.Entry<String, Class<? extends Entity>> entry = iter.next();
            Class<? extends Entity> clazz = entry.getValue();
            String name = entry.getKey();
            String className = clazz.getSimpleName();
            EntityRegistration er = EntityRegistry.instance().lookupModSpawn(clazz, true);

            if (er != null)
            {
                entityDump.addData(er.getContainer().getModId(), er.getContainer().getName(), name, className, String.valueOf(er.getModEntityId()));
            }
            else
            {
                entityDump.addData("minecraft", "Minecraft", name, className, String.valueOf(EntityList.getIDFromString(name)));
            }
        }

        entityDump.addTitle("Mod ID", "Mod name", "Entity name", "Entity class name", "ID");
        entityDump.setColumnAlignment(4, Alignment.RIGHT); // id
        entityDump.setUseColumnSeparator(true);

        return entityDump.getLines();
    }
}
