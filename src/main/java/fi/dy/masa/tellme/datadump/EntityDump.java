package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class EntityDump
{
    public static List<String> getFormattedEntityDump(@Nullable World world, DataDump.Format format, boolean includeClassName)
    {
        DataDump entityDump = new DataDump(includeClassName ? 5 : 4, format);

        for (Map.Entry<RegistryKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries())
        {
            EntityType<?> type = entry.getValue();
            ResourceLocation id = type.getRegistryName();
            String modName = ModNameUtils.getModName(id);
            @SuppressWarnings("deprecation")
            String entityId = String.valueOf(Registry.ENTITY_TYPE.getId(type));
            String category = type.getClassification().getName();

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
                catch (Exception ignore) {}

                entityDump.addData(modName, id.toString(), entityId, category, className);
            }
            else
            {
                entityDump.addData(modName, id.toString(), entityId, category);
            }
        }

        entityDump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // id

        if (includeClassName)
        {
            entityDump.addTitle("Mod name", "Registry name", "ID", "Category", "Entity class name");
        }
        else
        {
            entityDump.addTitle("Mod name", "Registry name", "ID", "Category");
        }

        return entityDump.getLines();
    }

    public static List<String> getFormattedEntityAttributeDump(DataDump.Format format)
    {
        DataDump dump = new DataDump(4, format);

        for (ResourceLocation id : ForgeRegistries.ATTRIBUTES.getKeys())
        {
            String idStr = id.toString();
            Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(id);
            String translationKey = attr.getAttributeName();
            String defaultValue = String.valueOf(attr.getDefaultValue());
            String tracked = String.valueOf(attr.getShouldWatch());

            dump.addData(idStr, translationKey, defaultValue, tracked);
        }

        dump.addTitle("Registry name", "Translation key", "Default", "Tracked");
        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // default
        dump.setColumnAlignment(3, DataDump.Alignment.RIGHT); // tracked

        return dump.getLines();
    }
}
