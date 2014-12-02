package fi.dy.masa.tellme.util;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import fi.dy.masa.tellme.TellMe;

public class EntityInfo
{
    public static void printBasicEntityInfoToChat(EntityPlayer player, Entity target)
    {
        for (String line : getBasicEntityInfo(target))
        {
            player.addChatMessage(new ChatComponentText(line));
        }
    }

    public static ArrayList<String> getBasicEntityInfo(Entity target)
    {
        ArrayList<String> lines = new ArrayList<String>();

        lines.add("Entity: " + target.getClass().getSimpleName());
        if (target instanceof EntityLivingBase)
        {
            EntityLivingBase lb = (EntityLivingBase)target;
            lines.add("Health: " + lb.getHealth());
        }

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
        DataDump.dumpDataToFile("entity_data", getFullEntityInfo(target));
    }
}
