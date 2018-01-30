package fi.dy.masa.tellme.proxy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import fi.dy.masa.tellme.datadump.BlockDump;
import fi.dy.masa.tellme.datadump.ItemDump;

public class CommonProxy
{
    public String getBiomeName(Biome biome)
    {
        try
        {
            String name = ReflectionHelper.getPrivateValue(Biome.class, biome, "field_76791_y", "biomeName");

            if (name != null)
            {
                return name;
            }
        }
        catch (Exception e) {}

        return "N/A";
    }

    @Nullable
    public Pair<Integer, Integer> getBiomeGrassAndFoliageColors(Biome biome)
    {
        return null;
    }

    public void getCurrentBiomeInfoClientSide(EntityPlayer player, Biome bgb) {}

    public void getDataForBlockSubtypes(Block block, ResourceLocation rl, BlockDump blockDump)
    {
        blockDump.addData(block, rl, false, false, ItemStack.EMPTY);
    }

    public void getDataForItemSubtypes(Item item, ResourceLocation rl, ItemDump itemDump)
    {
        if (item.getHasSubtypes())
        {
            itemDump.addData(item, rl, true, ItemStack.EMPTY);
        }
        else
        {
            itemDump.addData(item, rl, false, new ItemStack(item, 1, 0));
        }
    }

    public void getExtendedBlockStateInfo(World world, IBlockState state, BlockPos pos, List<String> lines)
    {
    }

    public Collection<Chunk> getLoadedChunks(World world)
    {
        IChunkProvider provider = world.getChunkProvider();

        if (provider instanceof ChunkProviderServer)
        {
            return ((ChunkProviderServer) provider).getLoadedChunks();
        }

        return Collections.emptyList();
    }

    public boolean isClient()
    {
        return false;
    }

    public boolean isSinglePlayer()
    {
        return false;
    }

    public void registerClientCommand() { }

    public void registerEventHandlers() { }
}
