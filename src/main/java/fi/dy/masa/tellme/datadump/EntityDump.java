package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityDump
{
    public static List<String> getFormattedEntityDump(@Nullable World world, DataDump.Format format, boolean includeClassName)
    {
        DataDump entityDump = new DataDump(includeClassName ? 4 : 3, format);

        for (Map.Entry<ResourceLocation, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries())
        {
            ResourceLocation id = entry.getKey();
            EntityType<?> type = entry.getValue();
            String modName = ModNameUtils.getModName(id);
            @SuppressWarnings("deprecation")
            String entityId = String.valueOf(Registry.ENTITY_TYPE.getId(type));

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
