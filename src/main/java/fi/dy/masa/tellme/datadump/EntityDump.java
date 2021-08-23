package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
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
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    className = clazz.getName();
                }
                catch (Exception ignore) {}

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

    public static List<String> getFormattedEntityAttributeDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(4, format);

        for (Identifier id : Registry.ATTRIBUTE.getIds())
        {
            String idStr = id.toString();
            EntityAttribute attr = Registry.ATTRIBUTE.get(id);
            String translationKey = attr.getTranslationKey();
            String defaultValue = String.valueOf(attr.getDefaultValue());
            String tracked = String.valueOf(attr.isTracked());

            dump.addData(idStr, translationKey, defaultValue, tracked);
        }

        dump.addTitle("Registry name", "Translation key", "Default", "Tracked");
        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // default
        dump.setColumnAlignment(3, DataDump.Alignment.RIGHT); // tracked

        return dump.getLines();
    }
}
