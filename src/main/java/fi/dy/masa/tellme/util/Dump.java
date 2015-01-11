package fi.dy.masa.tellme.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.EntityRegistry.EntityRegistration;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.ReflectionHelper;
import fi.dy.masa.tellme.TellMe;

public class Dump
{
    public static Dump instance = new Dump();
    public int longestModId = 0;
    public int longestModName = 0;
    public int longestName = 0;
    public int longestDisplayName = 0;

    public class Data implements Comparable<Data>
    {
        public String modId;
        public String modName;
        public String name;
        public String displayName;
        public int id;
        public boolean hasSubtypes;

        public Data(Block block)
        {
            this.name = Block.blockRegistry.getNameForObject(block);
            this.displayName = "";
            @SuppressWarnings("deprecation")
            ModContainer mod = GameData.findModOwner(this.name);
            this.modId = mod == null ? "minecraft" : mod.getModId();
            this.modName = mod == null ? "Minecraft" : mod.getName();
            this.id = Block.getIdFromBlock(block);
            Item item = Item.getItemFromBlock(block);
            this.hasSubtypes = (item != null && item.getHasSubtypes() == true);

            // Get the display name for items that have no sub types (ie. we know there is a valid item at damage = 0)
            if (this.hasSubtypes == false)
            {
                try
                {
                    Block block2 = (item instanceof ItemBlock && block.isFlowerPot() == false) ? Block.getBlockFromItem(item) : block;
                    ItemStack stack = new ItemStack(block2, 1, 0);
                    //ItemStack stack = new ItemStack(block.getItemDropped(0, new Random(), 0), 1, 0);
                    //ItemStack stack = new ItemStack(Item.getItemFromBlock(block), 1, 0);
                    //ItemStack stack = new ItemStack(block, 1, 0);
                    if (stack != null && stack.getItem() != null)
                    {
                        this.displayName = stack.getDisplayName();
                    }
                }
                catch (Exception e) {}
            }
        }

        public Data(Item item)
        {
            this.name = Item.itemRegistry.getNameForObject(item);
            this.displayName = "";
            @SuppressWarnings("deprecation")
            ModContainer mod = GameData.findModOwner(this.name);
            this.modId = mod == null ? "minecraft" : mod.getModId();
            this.modName = mod == null ? "Minecraft" : mod.getName();
            this.id = Item.getIdFromItem(item);
            this.hasSubtypes = item.getHasSubtypes();

            // Get the display name for items that have no sub types (ie. we know there is a valid item at damage = 0)
            if (this.hasSubtypes == false)
            {
                try
                {
                    ItemStack stack = new ItemStack(item, 1, 0);
                    if (stack != null && stack.getItem() != null)
                    {
                        this.displayName = stack.getDisplayName();
                    }
                }
                catch (Exception e) {}
            }
        }

        public Data(String entity, String dName, int id, String modId, String modName)
        {
            this.modId = modId;
            this.modName = modName;
            this.name = entity;
            this.displayName = dName;
            this.id = id;
        }

        public int compareTo(Data other)
        {
            int result = this.modName.compareTo(other.modName);
            if (result != 0)
            {
                return result;
            }

            return this.name.compareTo(other.name);
        }
    }

    public List<Data> getItemsOrBlocks(boolean isItem)
    {
        ArrayList<Data> list = new ArrayList<Data>();
        @SuppressWarnings("rawtypes")
        Iterator iter;

        if (isItem == true)
        {
            iter = GameData.getItemRegistry().iterator();
        }
        else
        {
            iter = GameData.getBlockRegistry().iterator();
        }

        this.longestModId = 0;
        this.longestModName = 0;
        this.longestName = 0;
        this.longestDisplayName = 0;

        Data data;
        while (iter.hasNext() == true)
        {
            if (isItem == true)
            {
                data = new Data((Item)iter.next());
            }
            else
            {
                data = new Data((Block)iter.next());
            }

            list.add(data);

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

        return list;
    }

    public List<String> getItemOrBlockDump(List<Data> list, boolean isItem)
    {
        Collections.sort(list);

        ArrayList<String> lines = new ArrayList<String>();
        String fmt = String.format("%%-%ds %%-%ds %%-%ds %%8d %%16s %%-%ds", this.longestModName, this.longestModId, this.longestName, this.longestDisplayName);
        String fmtTitle = String.format("%%-%ds %%-%ds %%-%ds %%8s %%16s %%-%ds", this.longestModName, this.longestModId, this.longestName, this.longestDisplayName);

        StringBuilder separator = new StringBuilder(256);
        int len = this.longestModId + this.longestModName + this.longestName + this.longestDisplayName + 29;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        lines.add(separator.toString());
        lines.add("*** WARNING ***");
        lines.add("The block and item IDs are dynamic and will be different on each world!");
        lines.add("DO NOT use them for anything \"proper\"!! (other than manual editing/fixing of raw world data or something)");
        lines.add("*** WARNING ***");
        lines.add(separator.toString());
        lines.add("*** WARNING ***");
        lines.add("The server doesn't have a list of the actual sub block and sub items.");
        lines.add("That is why the block and item list dumps only contain one entry per block and item class (separate ID).");
        lines.add("*** WARNING ***");
        lines.add(separator.toString());

        String typeName = isItem ? "Item" : "Block";
        lines.add(String.format(fmtTitle, "Mod Name", "Mod ID", typeName + " name", typeName + " ID", "| Has subtypes |", "Displayname"));

        lines.add(separator.toString());

        for (Data d : list)
        {
            if (d.hasSubtypes == true)
            {
                lines.add(String.format(fmt, d.modName, d.modId, d.name, d.id, "true", ""));
            }
            else
            {
                lines.add(String.format(fmt, d.modName, d.modId, d.name, d.id, "", d.displayName));
            }
        }

        return lines;
    }

    public List<String> getEntityDump()
    {
        ArrayList<Data> entities = new ArrayList<Data>();
        ArrayList<String> lines = new ArrayList<String>();
        this.longestModId = 0;
        this.longestModName = 0;
        this.longestName = 0;
        this.longestDisplayName = 0;

        Field idField = ReflectionHelper.findField(EntityList.class, "g", "field_75622_f", "stringToIDMapping");

        for (Object o : EntityList.stringToClassMapping.keySet())
        {
            String name = (String)o;
            int len = name.length();
            if (len > this.longestName)
            {
                this.longestName = len;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Entity> c = (Class<? extends Entity>)EntityList.stringToClassMapping.get(name);
            if (c != null)
            {
                len = c.getSimpleName().length();
                if (len > this.longestDisplayName)
                {
                    this.longestDisplayName = len;
                }

                EntityRegistration er = EntityRegistry.instance().lookupModSpawn(c, false);
                if (er != null)
                {
                    entities.add(new Data(name, c.getSimpleName(), er.getModEntityId(), er.getContainer().getModId(), er.getContainer().getName()));

                    len = er.getContainer().getModId().length();
                    if (len > this.longestModId)
                    {
                        this.longestModId = len;
                    }

                    len = er.getContainer().getName().length();
                    if (len > this.longestModName)
                    {
                        this.longestModName = len;
                    }
                }
                else
                {
                    try
                    {
                        @SuppressWarnings("unchecked")
                        int id = ((Integer)((HashMap<String, Integer>)idField.get(null)).get(name)).intValue();
                        entities.add(new Data(name, c.getSimpleName(), id, "minecraft", "Minecraft"));
                    }
                    catch (IllegalAccessException e)
                    {
                        entities.add(new Data(name, c.getSimpleName(), -1, "minecraft", "Minecraft"));
                        TellMe.logger.error("Error while trying to read Entity IDs");
                        e.printStackTrace();
                    }
                }
            }
        }

        Collections.sort(entities);
        String fmt = String.format("%%-%ds %%-%ds %%-%ds %%-%ds", this.longestModName, this.longestModId, this.longestName, this.longestDisplayName);

        StringBuilder separator = new StringBuilder(256);
        int len = this.longestModId + this.longestModName + this.longestName + this.longestDisplayName + 13;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        lines.add(String.format(fmt + " %s", "Mod Name", "Mod ID", "Entity Identifier", "Entity class name", "Entity ID"));
        lines.add(separator.toString());

        for (Data d : entities)
        {
            lines.add(String.format(fmt + " %9d", d.modName, d.modId, d.name, d.displayName, d.id));
        }

        return lines;
    }
}
