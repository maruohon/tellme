package fi.dy.masa.tellme.proxy;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import fi.dy.masa.tellme.util.GameObjectData;

public class CommonProxy
{
    public void getCurrentBiomeInfoClientSide(EntityPlayer player, Biome bgb) {}

    public void getBlockSubtypes(List<GameObjectData> list, Block block, ResourceLocation rl, int id)
    {
        list.add(new GameObjectData(rl, id, block));
    }

    public void getItemSubtypes(List<GameObjectData> list, Item item, ResourceLocation rl, int id)
    {
        if (item.getHasSubtypes())
        {
            list.add(new GameObjectData(rl, id, 0, item, true, null));
        }
        else
        {
            list.add(new GameObjectData(rl, id, 0, item, false, new ItemStack(item, 1, 0)));
        }
    }

    public void registerClientCommand() { }
}
