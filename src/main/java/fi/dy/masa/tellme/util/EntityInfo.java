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
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
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
        lines.add("");

        NBTFormatter.getPrettyFormattedNBT(lines, nbt);

        return lines;
    }

    public static void printBasicEntityInfoToChat(EntityPlayer player, Entity target)
    {
        ResourceLocation rl = EntityList.getKey(target);
        String regName = rl != null ? rl.toString() : "null";

        TextComponentString copy = new TextComponentString(regName);
        copy.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tellme copy-to-clipboard " + regName));
        copy.getStyle().setUnderlined(Boolean.valueOf(true));

        TextComponentString hoverText = new TextComponentString(String.format("Copy the string '%s' to clipboard", regName));
        copy.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

        TextComponentString full = new TextComponentString(String.format("Entity: %s [registry name: ", target.getName()));
        full.appendSibling(copy).appendText(String.format("] (entityId: %d)", target.getEntityId()));

        player.sendMessage(full);
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
