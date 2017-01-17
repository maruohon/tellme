package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;

public class EntityInfo
{
    private static List<String> getBasicEntityInfo(Entity target)
    {
        List<String> lines = new ArrayList<String>();

        ResourceLocation rl = EntityList.getKey(target);
        String regName = rl != null ? rl.toString() : "null";

        lines.add(String.format("Entity: %s [regName: %s] (entityId: %d)",
                target.getClass().getSimpleName(), regName, target.getEntityId()));

        return lines;
    }

    private static List<String> getFullEntityInfo(Entity target)
    {
        List<String> lines = getBasicEntityInfo(target);
        NBTTagCompound nbt = new NBTTagCompound();

        target.writeToNBT(nbt);
        NBTFormatter.getPrettyFormattedNBT(lines, nbt);

        return lines;
    }

    public static void printBasicEntityInfoToChat(EntityPlayer player, Entity target)
    {
        for (String line : getBasicEntityInfo(target))
        {
            player.sendMessage(new TextComponentString(line));
        }
    }

    public static void printFullEntityInfoToConsole(EntityPlayer player, Entity target)
    {
        List<String> lines = getFullEntityInfo(target);

        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void printEntityInfo(EntityPlayer player, Entity target, boolean dumpToFile)
    {
        EntityInfo.printBasicEntityInfoToChat(player, target);

        if (dumpToFile)
        {
            EntityInfo.dumpFullEntityInfoToFile(player, target);
        }
        else
        {
            EntityInfo.printFullEntityInfoToConsole(player, target);
        }
    }

    public static void dumpFullEntityInfoToFile(EntityPlayer player, Entity target)
    {
        File f = DataDump.dumpDataToFile("entity_data", getFullEntityInfo(target));
        player.sendMessage(new TextComponentString("Output written to file " + f.getName()));
    }

    public static String getEntityNameFromClass(Class<? extends Entity> clazz)
    {
        String name = null;
        ResourceLocation rl = EntityList.getKey(clazz);

        if (rl != null)
        {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(rl);

            if (entry != null)
            {
                name = entry.getName();
            }
        }

        if (name == null)
        {
            name = clazz.getSimpleName();
        }

        return name;
    }
}
