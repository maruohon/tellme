package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.ModNameUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class EntityDump
{
    public static List<String> getFormattedEntityDump(@Nullable Level world, DataDump.Format format, boolean includeClassName)
    {
        DataDump entityDump = new DataDump(includeClassName ? 5 : 4, format);

        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITY_TYPES.getEntries())
        {
            EntityType<?> type = entry.getValue();
            ResourceLocation id = entry.getKey().location();
            String modName = ModNameUtils.getModName(id);
            @SuppressWarnings("deprecation")
            String entityId = String.valueOf(Registry.ENTITY_TYPE.getId(type));
            String category = type.getCategory().getName();

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
            String translationKey = attr.getDescriptionId();
            String defaultValue = String.valueOf(attr.getDefaultValue());
            String tracked = String.valueOf(attr.isClientSyncable());

            dump.addData(idStr, translationKey, defaultValue, tracked);
        }

        dump.addTitle("Registry name", "Translation key", "Default", "Tracked");
        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // default
        dump.setColumnAlignment(3, DataDump.Alignment.RIGHT); // tracked

        return dump.getLines();
    }
}
