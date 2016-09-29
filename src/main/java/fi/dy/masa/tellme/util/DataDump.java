package fi.dy.masa.tellme.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;

public class DataDump
{
    private int longestModId = 0;
    private int longestModName = 0;
    private int longestName = 0;
    private int longestDisplayName = 0;

    private DataDump()
    {
        this.resetColumnWidths();
    }

    private void resetColumnWidths()
    {
        this.setColumnWidths(10, 10, 10, 5);
    }

    private void setColumnWidths(int idLen, int modNameLen, int nameLen, int dispNameLen)
    {
        this.longestModId = idLen;
        this.longestModName = modNameLen;
        this.longestName = nameLen;
        this.longestDisplayName = dispNameLen;
    }

    private void updateColumnWidths(List<GameObjectData> list)
    {
        for (GameObjectData data : list)
        {
            int len = data.getModId().length();
            if (len > this.longestModId)
            {
                this.longestModId = len;
            }

            len = data.getModName().length();
            if (len > this.longestModName)
            {
                this.longestModName = len;
            }

            len = data.getName().length();
            if (len > this.longestName)
            {
                this.longestName = len;
            }

            len = data.getDisplayName().length();
            if (len > this.longestDisplayName)
            {
                this.longestDisplayName = len;
            }
        }
    }

    private List<String> getFormattedDump(List<GameObjectData> list)
    {
        this.resetColumnWidths();
        this.updateColumnWidths(list);

        Collections.sort(list);
        List<String> lines = new ArrayList<String>();
        String fmt = String.format("%%-%ds  %%-%ds  %%-%ds %%8d %%10d %%9s  %%-%ds", this.longestModName, this.longestModId, this.longestName, this.longestDisplayName);
        String fmtTitle = String.format("%%-%ds  %%-%ds  %%-%ds %%8s %%10s %%9s  %%-%ds", this.longestModName, this.longestModId, this.longestName, this.longestDisplayName);

        StringBuilder separator = new StringBuilder(256);
        int len = this.longestModId + this.longestModName + this.longestName + this.longestDisplayName + 36;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        lines.add(separator.toString());
        lines.add("*** WARNING ***");
        lines.add("The block and item IDs are dynamic and will be different on each world!");
        lines.add("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
        lines.add(separator.toString());
        lines.add("*** WARNING ***");
        lines.add("The server doesn't have a list of sub block and sub items");
        lines.add("(= items with different damage value or blocks with different metadata).");
        lines.add("That is why the block and item list dumps only contain one entry per block/item class (separate ID) when run on a server.");
        lines.add("NOTE: The metadata value displayed is from the ItemStacks from getSubBlocks(), it's NOT necessarily the meta value in world!!");
        lines.add("NOTE: For blocks, Subtypes = true is only based on the number of returned ItemStacks from getSubBlocks() being > 1");
        lines.add("NOTE: For blocks, Subtypes = ? means that Item.getItemFromBlock(block) returned null or the command was run on the server side");

        lines.add(separator.toString());
        lines.add(String.format(fmtTitle, "Mod Name", "Mod ID", "Name", "ID", "Item Meta", "Subtypes", "Display Name"));
        lines.add(separator.toString());

        for (GameObjectData data : list)
        {
            String subtypes = data.areSubtypesKnown() == false ? "?" : (data.hasSubtypes() ? "true" : "false");

            lines.add(String.format(fmt, data.getModName(), data.getModId(), data.getName(), data.getId(), data.getMeta(), subtypes, data.getDisplayName()));
        }

        lines.add(separator.toString());
        lines.add(String.format(fmtTitle, "Mod Name", "Mod ID", "Name", "ID", "Item Meta", "Subtypes", "Display Name"));
        lines.add(separator.toString());

        return lines;
    }

    public static List<String> getFormattedBlockDump()
    {
        DataDump data = new DataDump();
        List<GameObjectData> list = new ArrayList<GameObjectData>();

        Iterator<Map.Entry<ResourceLocation, Block>> iter = ForgeRegistries.BLOCKS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Block> entry = iter.next();
            GameObjectData.getDataForBlock(entry.getValue(), entry.getKey(), list);
        }

        return data.getFormattedDump(list);
    }

    public static List<String> getFormattedItemDump()
    {
        DataDump data = new DataDump();
        List<GameObjectData> list = new ArrayList<GameObjectData>();

        Iterator<Map.Entry<ResourceLocation, Item>> iter = ForgeRegistries.ITEMS.getEntries().iterator();

        while (iter.hasNext())
        {
            Map.Entry<ResourceLocation, Item> entry = iter.next();
            GameObjectData.getDataForItem(entry.getValue(), entry.getKey(), list);
        }

        return data.getFormattedDump(list);
    }

    public static List<String> getEntityDump()
    {
        DataDump data = new DataDump();
        List<GameObjectData> entityData = new ArrayList<GameObjectData>();
        List<String> lines = new ArrayList<String>();

        for (String name : EntityList.NAME_TO_CLASS.keySet())
        {
            Class<? extends Entity> c = (Class<? extends Entity>)EntityList.NAME_TO_CLASS.get(name);
            if (c != null)
            {
                EntityRegistration er = EntityRegistry.instance().lookupModSpawn(c, true);
                if (er != null)
                {
                    entityData.add(new GameObjectData(name, c.getSimpleName(), er.getModEntityId(), er.getContainer().getModId(), er.getContainer().getName()));
                }
                else
                {
                    entityData.add(new GameObjectData(name, c.getSimpleName(), EntityList.getIDFromString(name), "minecraft", "Minecraft"));
                }
            }
        }

        data.setColumnWidths(10, 10, 18, 19);
        data.updateColumnWidths(entityData);
        Collections.sort(entityData);
        String fmt = String.format("%%-%ds %%-%ds %%-%ds %%-%ds", data.longestModName, data.longestModId, data.longestName, data.longestDisplayName);

        StringBuilder separator = new StringBuilder(256);
        int len = data.longestModId + data.longestModName + data.longestName + data.longestDisplayName + 13;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        lines.add(String.format(fmt + " %s", "Mod Name", "Mod ID", "Entity Identifier", "Entity class name", "Entity ID"));
        lines.add(separator.toString());

        for (GameObjectData d : entityData)
        {
            lines.add(String.format(fmt + " %9d", d.getModName(), d.getModId(), d.getName(), d.getDisplayName(), d.getId()));
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
