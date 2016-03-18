package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;

import fi.dy.masa.tellme.TellMe;

public class EntityInfo
{
    private static List<String> getBasicEntityInfo(Entity target)
    {
        List<String> lines = new ArrayList<String>();

        lines.add("Entity: " + target.getClass().getSimpleName() + " (entityId: " + target.getEntityId() + ")");

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
            player.addChatMessage(new TextComponentString(line));
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

    public static void dumpFullEntityInfoToFile(EntityPlayer player, Entity target)
    {
        File f = DataDump.dumpDataToFile("entity_data", getFullEntityInfo(target));
        player.addChatMessage(new TextComponentString("Output written to file " + f.getName()));
    }
}
