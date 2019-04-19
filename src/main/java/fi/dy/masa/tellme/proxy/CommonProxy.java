package fi.dy.masa.tellme.proxy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import fi.dy.masa.tellme.datadump.DataDump;

public class CommonProxy
{
    public String getBiomeName(Biome biome)
    {
        try
        {
            String name = ObfuscationReflectionHelper.getPrivateValue(Biome.class, biome, "field_76791_y"); // biomeName

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

    public void addCreativeTabData(DataDump dump) {}

    public void addCreativeTabNames(JsonObject obj, Item item) {}

    public void addMusicTypeData(DataDump dump) {}

    public void getCurrentBiomeInfoClientSide(EntityPlayer player, Biome bgb) {}

    @Nullable
    public Iterable<Advancement> getAdvacements(ICommandSender sender)
    {
        World world = sender.getEntityWorld();

        return (world instanceof WorldServer) ? ((WorldServer) world).getAdvancementManager().getAdvancements() : null;
    }

    public ICommandManager getCommandHandler()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
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
