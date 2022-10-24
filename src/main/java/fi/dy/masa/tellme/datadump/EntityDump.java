package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;

public class EntityDump
{
    public static List<String> getFormattedEntityDump(Format format)
    {
        DataDump entityDump = new DataDump(5, format);

        for (ResourceLocation key : EntityList.REGISTRY.getKeys())
        {
            Class<? extends Entity> clazz = EntityList.REGISTRY.getObject(key);
            String className = clazz.getSimpleName();
            String oldName = EntityList.getTranslationName(key);

            entityDump.addData("Minecraft", key.toString(), oldName, className, String.valueOf(EntityList.REGISTRY.getIDForObject(clazz)));
        }

        entityDump.addTitle("Mod name", "Registry name", "Old name", "Entity class name", "ID");
        entityDump.setColumnProperties(4, Alignment.RIGHT, true); // id
        entityDump.setUseColumnSeparator(true);

        return entityDump.getLines();
    }
}
