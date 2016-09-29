package fi.dy.masa.tellme.util;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import fi.dy.masa.tellme.TellMe;

public class GameObjectData implements Comparable<GameObjectData>
{
    private String modId;
    private String modName;
    private String name;
    private String displayName;
    private int id;
    private int meta;
    private boolean hasSubtypes;
    private boolean subtypesKnown = true;

    public GameObjectData(String name, String dName, int id, String modId, String modName)
    {
        this.modId = modId;
        this.modName = modName;
        this.name = name;
        this.displayName = dName;
        this.id = id;
    }

    public GameObjectData(ResourceLocation rl, int id, Block block)
    {
        this(rl, id, 0, block, false, null);

        this.subtypesKnown = false;
    }

    public GameObjectData(ResourceLocation rl, int id, int meta, Block block, boolean hasSubTypes, @Nullable ItemStack stack)
    {
        this(rl, id, meta, Item.getItemFromBlock(block), hasSubTypes, stack);
    }

    public GameObjectData(ResourceLocation rl, int id, int meta, Item item, boolean hasSubTypes, @Nullable ItemStack stack)
    {
        this.displayName = "";
        this.id = id;
        this.meta = meta;
        this.hasSubtypes = hasSubTypes;

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

        // Get the display name for items that we know a valid metadata/ItemStack for
        if (stack != null)
        {
            this.displayName = stack.getDisplayName();
        }
    }

    @Override
    public int compareTo(GameObjectData other)
    {
        int result = this.modId.compareTo(other.modId);
        if (result != 0)
        {
            return result;
        }

        return this.name.compareTo(other.name);
    }

    public String getModId()
    {
        return this.modId;
    }

    public String getModName()
    {
        return this.modName;
    }

    public String getName()
    {
        return this.name;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public int getId()
    {
        return this.id;
    }

    public int getMeta()
    {
        return this.meta;
    }

    public boolean areSubtypesKnown()
    {
        return this.subtypesKnown;
    }

    public boolean hasSubtypes()
    {
        return this.hasSubtypes;
    }

    public static void getDataForBlock(Block block, ResourceLocation rl, List<GameObjectData> list)
    {
        if (block != null)
        {
            int id = Block.getIdFromBlock(block);

            TellMe.proxy.getBlockSubtypes(list, block, rl, id);
        }

    }

    public static void getDataForItem(Item item, ResourceLocation rl, List<GameObjectData> list)
    {
        if (item != null)
        {
            int id = Item.getIdFromItem(item);

            TellMe.proxy.getItemSubtypes(list, item, rl, id);
        }
    }
}
