package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import fi.dy.masa.tellme.TellMe;

public class EntityInfo
{
    public static ArrayList<String> getBasicEntityInfo(Entity target)
    {
        ArrayList<String> lines = new ArrayList<String>();

        lines.add("Entity: " + target.getClass().getSimpleName() + " (entityId: " + target.getEntityId() + ")");

        return lines;
    }

    public static ArrayList<String> getFullEntityInfo(Entity target)
    {
        ArrayList<String> lines = getBasicEntityInfo(target);
        NBTTagCompound nbt = new NBTTagCompound();

        target.writeToNBT(nbt);
        NBTFormatter.NBTFormatterPretty(lines, nbt);

        return lines;
    }

    public static void printBasicEntityInfoToChat(EntityPlayer player, Entity target)
    {
        for (String line : getBasicEntityInfo(target))
        {
            player.addChatMessage(new ChatComponentText(line));
        }
    }

    public static void printEntityInfoToConsole(EntityPlayer player, Entity target)
    {
        ArrayList<String> lines = getFullEntityInfo(target);

        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void dumpEntityInfoToFile(EntityPlayer player, Entity target)
    {
        File f = DataDump.dumpDataToFile("entity_data", getFullEntityInfo(target));
        player.addChatMessage(new ChatComponentText("Output written to file " + f.getName()));
    }
}
