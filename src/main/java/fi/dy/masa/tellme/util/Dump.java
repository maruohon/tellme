package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;

public class Dump
{
    public static Dump instance = new Dump();
    public int longestModId = 0;
    public int longestModName = 0;
    public int longestName = 0;

    public class Data implements Comparable<Data>
    {
        public String modId;
        public String modName;
        public String name;
        public String displayName;
        public int id;

        public Data(Block block)
        {
            this.name = Block.blockRegistry.getNameForObject(block);
            @SuppressWarnings("deprecation")
            ModContainer mod = GameData.findModOwner(this.name);
            this.modId = mod == null ? "minecraft" : mod.getModId();
            this.modName = mod == null ? "Minecraft" : mod.getName();
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
            this.name = Item.itemRegistry.getNameForObject(item);
            @SuppressWarnings("deprecation")
            ModContainer mod = GameData.findModOwner(this.name);
            this.modId = mod == null ? "minecraft" : mod.getModId();
            this.modName = mod == null ? "Minecraft" : mod.getName();
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

    public List<Data> getBlocks()
    {
        ArrayList<Data> blocks = new ArrayList<Data>();
        @SuppressWarnings("unchecked")
        Iterator<Block> iter = GameData.getBlockRegistry().iterator();

        this.longestModId = 0;
        this.longestModName = 0;
        this.longestName = 0;

        while (iter.hasNext() == true)
        {
            Data bd = new Data(iter.next());
            blocks.add(bd);

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

        return blocks;
    }

    public List<Data> getItems()
    {
        ArrayList<Data> items = new ArrayList<Data>();
        @SuppressWarnings("unchecked")
        Iterator<Item> iter = GameData.getItemRegistry().iterator();

        this.longestModId = 0;
        this.longestModName = 0;
        this.longestName = 0;

        while (iter.hasNext() == true)
        {
            Data bd = new Data(iter.next());
            items.add(bd);

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

        return items;
    }

    public List<String> getDump(List<Data> list)
    {
        Collections.sort(list);

        ArrayList<String> lines = new ArrayList<String>();
        String fmt = String.format("%%-%ds %%-%ds %%-%ds", this.longestModName, this.longestModId, this.longestName);

        StringBuilder separator = new StringBuilder(256);
        int len = this.longestModId + this.longestModName + this.longestName + 12;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        lines.add(String.format(fmt + " %s", "Mod Name", "Mod ID", "Block/Item name", "Block/Item ID"));
        lines.add(separator.toString());

        for (Data d : list)
        {
            lines.add(String.format(fmt + " (%5d)", d.modName, d.modId, d.name, d.id));
        }

        return lines;
    }
}
