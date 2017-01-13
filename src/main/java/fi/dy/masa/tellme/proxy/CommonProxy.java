package fi.dy.masa.tellme.proxy;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import fi.dy.masa.tellme.datadump.BlockDump;
import fi.dy.masa.tellme.datadump.ItemDump;

public class CommonProxy
{
    public void getCurrentBiomeInfoClientSide(EntityPlayer player, Biome bgb) {}

    public void getDataForBlockSubtypes(Block block, ResourceLocation rl, BlockDump blockDump)
    {
        blockDump.addData(block, rl, false, false, null);
    }

    public void getDataForItemSubtypes(Item item, ResourceLocation rl, ItemDump itemDump)
    {
        if (item.getHasSubtypes())
        {
            itemDump.addData(item, rl, true, null);
        }
        else
        {
            itemDump.addData(item, rl, false, new ItemStack(item, 1, 0));
        }
    }

    public void getExtendedBlockStateInfo(World world, IBlockState state, BlockPos pos, List<String> lines)
    {
    }

    public void registerClientCommand() { }

    public void registerEventHandlers() { }
}
