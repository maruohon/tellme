package fi.dy.masa.tellme.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;

import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import fi.dy.masa.tellme.TellMe;

public class DataDump
{
    public int longestModId = 0;
    public int longestModName = 0;
    public int longestName = 0;
    public int longestDisplayName = 0;

    public DataDump()
    {
        this.resetColumnWidths();
    }

    public void resetColumnWidths()
    {
        this.setColumnWidths(10, 10, 10, 5);
    }

    public void setColumnWidths(int idLen, int modNameLen, int nameLen, int dispNameLen)
    {
        this.longestModId = idLen;
        this.longestModName = modNameLen;
        this.longestName = nameLen;
        this.longestDisplayName = dispNameLen;
    }

    public void updateColumnWidths(List<GameObjectData> list)
    {
        for (GameObjectData data : list)
        {
            int len = data.modId.length();
            if (len > this.longestModId)
            {
                this.longestModId = len;
            }

            len = data.modName.length();
            if (len > this.longestModName)
            {
                this.longestModName = len;
            }

            len = data.name.length();
            if (len > this.longestName)
            {
                this.longestName = len;
            }

            len = data.displayName.length();
            if (len > this.longestDisplayName)
            {
                this.longestDisplayName = len;
            }
        }
    }

    public List<String> getFormattedBlockDump()
    {
        Iterator<Block> iter = Block.blockRegistry.iterator();
        List<GameObjectData> list = new ArrayList<GameObjectData>();

        GameObjectData data;
        while (iter.hasNext() == true)
        {
            data = new GameObjectData(iter.next());
            list.add(data);
        }

        return this.getFormattedDump(list);
    }

    public List<String> getFormattedItemDump()
    {
        Iterator<Item> iter = Item.itemRegistry.iterator();
        List<GameObjectData> list = new ArrayList<GameObjectData>();

        GameObjectData data;
        while (iter.hasNext() == true)
        {
            data = new GameObjectData(iter.next());
            list.add(data);
        }

        return this.getFormattedDump(list);
    }

    public List<String> getFormattedDump(List<GameObjectData> list)
    {
        this.resetColumnWidths();
        this.updateColumnWidths(list);

        Collections.sort(list);
        List<String> lines = new ArrayList<String>();
        String fmt = String.format("%%-%ds %%-%ds %%-%ds %%8d %%14s   %%-%ds", this.longestModName, this.longestModId, this.longestName, this.longestDisplayName);
        String fmtTitle = String.format("%%-%ds %%-%ds %%-%ds %%8s %%16s %%-%ds", this.longestModName, this.longestModId, this.longestName, this.longestDisplayName);

        StringBuilder separator = new StringBuilder(256);
        int len = this.longestModId + this.longestModName + this.longestName + this.longestDisplayName + 29;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        lines.add(separator.toString());
        lines.add("*** WARNING ***");
        lines.add("The block and item IDs are dynamic and will be different on each world!");
        lines.add("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
        lines.add(separator.toString());
        lines.add("*** WARNING ***");
        lines.add("The server doesn't have a list of sub block and sub items");
        lines.add("(= items with different damage value or blocks with different metadata).");
        lines.add("That is why the block and item list dumps only contain one entry per block/item class (separate ID).");
        lines.add(separator.toString());

        lines.add(String.format(fmtTitle, "Mod Name", "Mod ID", "Name", "ID", "| Has subtypes |", "Display Name"));

        lines.add(separator.toString());

        for (GameObjectData data : list)
        {
            if (data.hasSubtypes == true)
            {
                lines.add(String.format(fmt, data.modName, data.modId, data.name, data.id, "true", ""));
            }
            else
            {
                lines.add(String.format(fmt, data.modName, data.modId, data.name, data.id, "-", data.displayName));
            }
        }

        return lines;
    }

    public List<String> getEntityDump()
    {
        List<GameObjectData> entityData = new ArrayList<GameObjectData>();
        List<String> lines = new ArrayList<String>();
        this.longestModId = 0;
        this.longestModName = 0;
        this.longestName = 0;
        this.longestDisplayName = 0;

        Field idField = ReflectionHelper.findField(EntityList.class, "g", "field_180126_g", "stringToIDMapping");

        for (String name : EntityList.stringToClassMapping.keySet())
        {
            Class<? extends Entity> c = (Class<? extends Entity>)EntityList.stringToClassMapping.get(name);
            if (c != null)
            {
                EntityRegistration er = EntityRegistry.instance().lookupModSpawn(c, true);
                if (er != null)
                {
                    entityData.add(new GameObjectData(name, c.getSimpleName(), er.getModEntityId(), er.getContainer().getModId(), er.getContainer().getName()));
                }
                else
                {
                    try
                    {
                        @SuppressWarnings("unchecked")
                        int id = ((Integer)((HashMap<String, Integer>)idField.get(null)).get(name)).intValue();
                        entityData.add(new GameObjectData(name, c.getSimpleName(), id, "minecraft", "Minecraft"));
                    }
                    catch (IllegalAccessException e)
                    {
                        entityData.add(new GameObjectData(name, c.getSimpleName(), -1, "minecraft", "Minecraft"));
                        TellMe.logger.error("getEntityDump(): Error while trying to read Entity IDs");
                        //e.printStackTrace();
                    }
                }
            }
        }

        this.setColumnWidths(10, 10, 18, 19);
        this.updateColumnWidths(entityData);
        Collections.sort(entityData);
        String fmt = String.format("%%-%ds %%-%ds %%-%ds %%-%ds", this.longestModName, this.longestModId, this.longestName, this.longestDisplayName);

        StringBuilder separator = new StringBuilder(256);
        int len = this.longestModId + this.longestModName + this.longestName + this.longestDisplayName + 13;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        lines.add(String.format(fmt + " %s", "Mod Name", "Mod ID", "Entity Identifier", "Entity class name", "Entity ID"));
        lines.add(separator.toString());

        for (GameObjectData d : entityData)
        {
            lines.add(String.format(fmt + " %9d", d.modName, d.modId, d.name, d.displayName, d.id));
        }

        return lines;
    }

    public static File dumpDataToFile(String fileNameBase, List<String> lines)
    {
        File outFile = null;

        File cfgDir = new File(TellMe.configDirPath);
        if (cfgDir.exists() == false)
        {
            try
            {
                cfgDir.mkdirs();
            }
            catch (Exception e)
            {
                TellMe.logger.error("dumpDataToFile(): Failed to create the configuration directory.");
                e.printStackTrace();
                return null;
            }

        }

        String fileNameBaseWithDate = fileNameBase + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date(System.currentTimeMillis()));
        String fileName = fileNameBaseWithDate + ".txt";
        outFile = new File(cfgDir, fileName);
        int postFix = 1;

        while (outFile.exists() == true)
        {
            fileName = fileNameBaseWithDate + "_" + postFix + ".txt";
            outFile = new File(cfgDir, fileName);
            postFix++;
        }

        try
        {
            outFile.createNewFile();
        }
        catch (IOException e)
        {
            TellMe.logger.error("dumpDataToFile(): Failed to create data dump file '" + fileName + "'");
            e.printStackTrace();
            return null;
        }

        try
        {
            for (int i = 0; i < lines.size(); ++i)
            {
                FileUtils.writeStringToFile(outFile, lines.get(i) + System.getProperty("line.separator"), true);
            }
        }
        catch (IOException e)
        {
            TellMe.logger.error("dumpDataToFile(): Exception while writing data dump to file '" + fileName + "'");
            e.printStackTrace();
        }

        return outFile;
    }
}
