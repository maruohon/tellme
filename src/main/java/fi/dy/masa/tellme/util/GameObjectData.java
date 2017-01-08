package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.tellme.TellMe;

public class GameObjectData implements Comparable<GameObjectData>
{
    private static final String EMPTY = "";

    private String modId;
    private String modName;
    private String name;
    private String displayName;
    private String oredictKeys = EMPTY;
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
            TellMe.logger.warn("ResourceLocation was null while identifying a block or item: '" + item + "' (id: " + id + ")");
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

        this.oredictKeys = getOredictKeysJoined(stack);
    }

    public static String getOredictKeysJoined(@Nullable ItemStack stack)
    {
        if (stack == null)
        {
            return EMPTY;
        }

        StringBuilder str = new StringBuilder(128);
        int[] ids = OreDictionary.getOreIDs(stack);

        if (ids.length == 0)
        {
            return EMPTY;
        }

        List<String> names = new ArrayList<String>();

        for (int id : ids)
        {
            names.add(OreDictionary.getOreName(id));
        }

        if (names.size() == 1)
        {
            return names.get(0);
        }

        Collections.sort(names);
        str.append(names.get(0));

        for (int i = 1; i < names.size(); i++)
        {
            str.append(", ").append(names.get(i));
        }

        return str.toString();
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

    public String getOreDictKeys()
    {
        return this.oredictKeys;
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
