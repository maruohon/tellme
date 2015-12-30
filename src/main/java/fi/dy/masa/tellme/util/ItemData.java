package fi.dy.masa.tellme.util;

import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.TellMe;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class ItemData implements Comparable<ItemData>
{
    public String modId;
    public String modName;
    public String name;
    public String displayName;
    public int id;
    public boolean hasSubtypes;

    public ItemData(Block block)
    {
        this(Block.blockRegistry.getNameForObject(block), Block.getIdFromBlock(block), Item.getItemFromBlock(block));
    }

    public ItemData(Item item)
    {
        this(Item.itemRegistry.getNameForObject(item), Item.getIdFromItem(item), item);
    }

    public ItemData(String name, String dName, int id, String modId, String modName)
    {
        this.modId = modId;
        this.modName = modName;
        this.name = name;
        this.displayName = dName;
        this.id = id;
    }

    public ItemData(ResourceLocation rl, int id, Item item)
    {
        this.displayName = "";
        this.id = id;
        this.hasSubtypes = item != null && item.getHasSubtypes();

        if (rl == null)
        {
            this.modId = "null";
            this.modName = "null";
            this.name = "" + item;
            TellMe.logger.warn("ResourceLocation was null while identifying a block or item: " + item + " (id: " + id + ")");
        }
        else
        {
            this.modId = rl.getResourceDomain();
            this.name = rl.getResourcePath();

            Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
            if (mods != null && mods.get(this.modId) != null)
            {
                this.modName = mods.get(this.modId).getName();
            }
            else
            {
                this.modName = "Minecraft";
            }
        }

        // Get the display name for items that have no sub types (ie. we know there is a valid item at damage = 0)
        if (this.hasSubtypes == false && item != null)
        {
            ItemStack stack = new ItemStack(item, 1, 0);
            if (stack != null && stack.getItem() != null)
            {
                this.displayName = stack.getDisplayName();
            }
        }
    }

    @Override
    public int compareTo(ItemData other)
    {
        int result = this.modId.compareTo(other.modId);
        if (result != 0)
        {
            return result;
        }

        return this.name.compareTo(other.name);
    }
}
