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
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
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

        public Data(Block block)
        {
            UniqueIdentifier ui = GameRegistry.findUniqueIdentifierFor(block);
            this.name = ui.name;
            this.modId = ui.modId;
            this.modName = "TODO";
            this.id = Block.getIdFromBlock(block);

            try
            {
                ItemStack stack = new ItemStack(block, 1, 0); // FIXME is there any point in this for just meta 0?
                if (stack != null && stack.getItem() != null)
                {
                    this.displayName = stack.getDisplayName();
                }
                else
                {
                    this.displayName = this.name;
                }
            }
            catch (Exception e) {}
        }

        public Data(Item item)
        {
            UniqueIdentifier ui = GameRegistry.findUniqueIdentifierFor(item);
            this.name = ui.name;
            this.modId = ui.modId;
            this.modName = "TODO";
            this.id = Item.getIdFromItem(item);

            try
            {
                ItemStack stack = new ItemStack(item, 1, 0); // FIXME is there any point in this for just damage 0?
                if (stack != null && stack.getItem() != null)
                {
                    this.displayName = stack.getDisplayName();
                }
                else
                {
                    this.displayName = this.name;
                }
            }
            catch (Exception e) {}
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

        Data bd;
        while (iter.hasNext() == true)
        {
            if (isItem == true)
            {
                bd = new Data((Item)iter.next());
            }
            else
            {
                bd = new Data((Block)iter.next());
            }

            list.add(bd);

            int len = bd.modId.length();
            if (len > this.longestModId)
            {
                this.longestModId = len;
            }

            len = bd.modName.length();
            if (len > this.longestModName)
            {
                this.longestModName = len;
            }

            len = bd.name.length();
            if (len > this.longestName)
            {
                this.longestName = len;
            }
        }

        return list;
    }

    public List<String> getItemOrBlockDump(List<Data> list, boolean isItem)
    {
        Collections.sort(list);

        ArrayList<String> lines = new ArrayList<String>();
        String fmt = String.format("%%-%ds %%-%ds %%-%ds", this.longestModName, this.longestModId, this.longestName);

        StringBuilder separator = new StringBuilder(256);
        int len = this.longestModId + this.longestModName + this.longestName + 11;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        if (isItem == true)
        {
            lines.add(String.format(fmt + " %s", "Mod Name", "Mod ID", "Item name", " Item ID"));
        }
        else
        {
            lines.add(String.format(fmt + " %s", "Mod Name", "Mod ID", "Block name", "Block ID"));
        }

        lines.add(separator.toString());

        for (Data d : list)
        {
            lines.add(String.format(fmt + " %8d", d.modName, d.modId, d.name, d.id));
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
                        TellMe.logger.error("Error while trying to read Entity IDs");
                        entities.add(new Data(name, c.getSimpleName(), -1, "minecraft", "Minecraft"));
                        //e.printStackTrace();
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
