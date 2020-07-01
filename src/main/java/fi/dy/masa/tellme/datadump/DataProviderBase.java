package fi.dy.masa.tellme.datadump;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils;
import fi.dy.masa.tellme.mixin.IMixinThreadedAnvilChunkStorage;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

public class DataProviderBase
{
    public File getConfigDirectory()
    {
        return new File("config");
    }

    @Nullable
    public Collection<Advancement> getAdvancements(MinecraftServer server)
    {
        return server != null ? server.getAdvancementManager().getAdvancements() : null;
    }

    public void getCurrentBiomeInfoClientSide(Entity entity, Biome biome)
    {
    }

    public World getWorld(@Nullable MinecraftServer server, DimensionType dimensionType) throws CommandSyntaxException
    {
        String name = Registry.DIMENSION.getId(dimensionType).toString();

        if (server == null)
        {
            throw CommandUtils.DIMENSION_NOT_LOADED_EXCEPTION.create(name);
        }

        World world = server.getWorld(dimensionType);

        if (world == null)
        {
            throw CommandUtils.DIMENSION_NOT_LOADED_EXCEPTION.create(name);
        }

        return world;
    }

    public int getFoliageColor(Biome biome, BlockPos pos)
    {
        double temperature = MathHelper.clamp(biome.getTemperature(pos), 0.0F, 1.0F);
        double humidity = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
        return FoliageColors.getColor(temperature, humidity);
    }

    public int getGrassColor(Biome biome, BlockPos pos)
    {
        double temperature = MathHelper.clamp(biome.getTemperature(pos), 0.0F, 1.0F);
        double humidity = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
        return GrassColors.getColor(temperature, humidity);
    }

    public Collection<WorldChunk> getLoadedChunks(World world)
    {
        if (world instanceof ServerWorld)
        {
            ServerWorld serverWorld = (ServerWorld) world; 
            ArrayList<WorldChunk> chunks = new ArrayList<>();

            try
            {
                Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = ((IMixinThreadedAnvilChunkStorage) serverWorld.getChunkManager().threadedAnvilChunkStorage).getChunkHolders();

                for (ChunkHolder holder : chunkHolders.values())
                {
                    Optional<WorldChunk> optional = holder.method_20725().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).left();

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
        return (new TranslatableText(biome.getTranslationKey())).getString();
    }

    public void addCommandDumpData(DataDump dump, MinecraftServer server)
    {
        if (server != null)
        {
            CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();

            for (CommandNode<ServerCommandSource> cmd : dispatcher.getRoot().getChildren())
            {
                String cmdName = cmd.getName();
                Command<ServerCommandSource> command = cmd.getCommand();
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
