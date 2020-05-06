package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.GrassColors;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class DataProviderBase
{
    private static final Field field_ChunkManager_immutableLoadedChunks = ObfuscationReflectionHelper.findField(ChunkManager.class, "field_219252_f");

    @Nullable
    public Collection<Advancement> getAdvacements()
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getAdvancementManager().getAllAdvancements() : null;
    }

    public void getCurrentBiomeInfoClientSide(Entity entity, Biome biome)
    {
    }

    public World getWorld(@Nullable MinecraftServer server, DimensionType dimensionType) throws CommandSyntaxException
    {
        if (server == null)
        {
            throw CommandUtils.DIMENSION_NOT_LOADED_EXCEPTION.create(dimensionType.getRegistryName().toString());
        }

        World world = DimensionManager.getWorld(server, dimensionType, false, false);

        if (world == null)
        {
            throw CommandUtils.DIMENSION_NOT_LOADED_EXCEPTION.create(dimensionType.getRegistryName().toString());
        }

        return world;
    }

    public int getFoliageColor(Biome biome, BlockPos pos)
    {
        double temperature = MathHelper.clamp(biome.func_225486_c(pos), 0.0F, 1.0F);
        double humidity = MathHelper.clamp(biome.getDownfall(), 0.0F, 1.0F);
        return FoliageColors.get(temperature, humidity);
    }

    public int getGrassColor(Biome biome, BlockPos pos)
    {
        double temperature = MathHelper.clamp(biome.func_225486_c(pos), 0.0F, 1.0F);
        double humidity = MathHelper.clamp(biome.getDownfall(), 0.0F, 1.0F);
        return GrassColors.get(temperature, humidity);
    }

    public Collection<Chunk> getLoadedChunks(World world)
    {
        if (world instanceof ServerWorld)
        {
            ArrayList<Chunk> chunks = new ArrayList<>();

            try
            {
                @SuppressWarnings("unchecked")
                Long2ObjectLinkedOpenHashMap<ChunkHolder> immutableLoadedChunks = (Long2ObjectLinkedOpenHashMap<ChunkHolder>) field_ChunkManager_immutableLoadedChunks.get(((ServerWorld) world).getChunkProvider().chunkManager);

                for (ChunkHolder holder : immutableLoadedChunks.values())
                {
                    Optional<Chunk> optional = holder.func_219297_b().getNow(ChunkHolder.UNLOADED_CHUNK).left();

                    if (optional.isPresent())
                    {
                        chunks.add(optional.get());
                    }
                }
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Failed to get the loaded chunks", e);
            }

            return chunks;
        }

        return Collections.emptyList();
    }

    public String getBiomeName(Biome biome)
    {
        return (new TranslationTextComponent(biome.getTranslationKey())).getString();
    }

    public void getExtendedBlockStateInfo(World world, BlockState state, BlockPos pos, List<String> lines)
    {
    }

    public void addCommandDumpData(DataDump dump)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server != null)
        {
            CommandDispatcher<CommandSource> dispatcher = server.getCommandManager().getDispatcher();

            for (CommandNode<CommandSource> cmd : dispatcher.getRoot().getChildren())
            {
                String cmdName = cmd.getName();
                Command<CommandSource> command = cmd.getCommand();
                String commandClassName = command != null ? command.getClass().getName() : "-";
                dump.addData(cmdName, commandClassName);
            }
        }
    }

    public void addItemGroupData(DataDump dump)
    {
    }

    public void addItemGroupNames(JsonObject obj, Item item)
    {
    }

    public void addMusicTypeData(DataDump dump)
    {
    }
}
