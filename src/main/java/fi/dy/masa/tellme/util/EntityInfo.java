package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.LiteModTellMe;
import fi.dy.masa.tellme.command.SubCommand;
import fi.dy.masa.tellme.datadump.DataDump;

public class EntityInfo
{
    private static String getBasicEntityInfo(Entity target)
    {
        ResourceLocation rl = EntityList.getKey(target);
        String regName = rl != null ? rl.toString() : "null";

        return String.format("Entity: %s [registry name: %s] (entityId: %d)", target.getName(), regName, target.getEntityId());
    }

    private static List<String> getFullEntityInfo(Entity target)
    {
        List<String> lines = new ArrayList<>();
        lines.add(getBasicEntityInfo(target));

        NBTTagCompound nbt = new NBTTagCompound();

        if (target.writeToNBTOptional(nbt) == false)
        {
            target.writeToNBT(nbt);
        }

        lines.add("Entity class: " + target.getClass().getName());
        lines.add("Entity UUID: " + target.getUniqueID().toString());
        lines.add("");

        NBTFormatter.getPrettyFormattedNBT(lines, nbt);

        lines.add("");
        lines.add(nbt.toString());

        return lines;
    }

    public static void printBasicEntityInfoToChat(EntityPlayer player, Entity target)
    {
        ResourceLocation rl = EntityList.getKey(target);
        String regName = rl != null ? rl.toString() : "null";
        String textPre = String.format("Entity: %s [registry name: ", target.getName());
        String textPost = String.format("] (entityId: %d)", target.getEntityId());

        player.sendMessage(ChatUtils.getClipboardCopiableMessage(textPre, regName, textPost));
    }

    public static void printFullEntityInfoToConsole(EntityPlayer player, Entity target)
    {
        List<String> lines = getFullEntityInfo(target);

        for (String line : lines)
        {
            LiteModTellMe.logger.info(line);
        }
    }

    public static void printEntityInfo(EntityPlayer player, Entity target, boolean dumpToFile)
    {
        EntityInfo.printBasicEntityInfoToChat(player, target);

        if (dumpToFile)
        {
            dumpFullEntityInfoToFile(player, target);
        }
        else
        {
            printFullEntityInfoToConsole(player, target);
        }
    }

    public static void dumpFullEntityInfoToFile(EntityPlayer player, Entity target)
    {
        File file = DataDump.dumpDataToFile("entity_data", getFullEntityInfo(target));
        SubCommand.sendClickableLinkMessage(player, "Output written to file %s", file);
    }

    public static String getEntityNameFromClass(Class<? extends Entity> clazz)
    {
        String name = null;
        ResourceLocation rl = EntityList.getKey(clazz);

        if (rl != null)
        {
            name = rl.toString();
        }

        if (name == null)
        {
            name = clazz.getSimpleName();
        }

        return name;
    }
}
