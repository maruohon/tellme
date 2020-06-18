package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class EntityDump
{
    public static List<String> getFormattedEntityDump(@Nullable World world, DataDump.Format format, boolean includeClassName)
    {
        DataDump entityDump = new DataDump(includeClassName ? 4 : 3, format);

        for (Identifier id : Registry.ENTITY_TYPE.getIds())
        {
            EntityType<?> type = Registry.ENTITY_TYPE.get(id);
            String modName = ModNameUtils.getModName(id);
            String entityId = String.valueOf(Registry.ENTITY_TYPE.getRawId(type));

            if (includeClassName && world != null)
            {
                String className = "?";

                try
                {
                    Entity entity = type.create(world);
                    Class<? extends Entity> clazz = entity.getClass();
                    entity.remove();
                    className = clazz.getName();
                }
                catch (Exception e) {}

                entityDump.addData(modName, id.toString(), className, entityId);
            }
            else
            {
                entityDump.addData(modName, id.toString(), entityId);
            }
        }

        if (includeClassName)
        {
            entityDump.addTitle("Mod name", "Registry name", "Entity class name", "ID");
            entityDump.setColumnProperties(3, DataDump.Alignment.RIGHT, true); // id
        }
        else
        {
            entityDump.addTitle("Mod name", "Registry name", "ID");
            entityDump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // id
        }

        return entityDump.getLines();
    }
}
