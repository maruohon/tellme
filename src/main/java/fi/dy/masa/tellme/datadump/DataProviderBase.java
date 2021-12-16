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
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.TellMe;
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
        return server != null ? server.getAdvancementLoader().getAdvancements() : null;
    }

    public void getCurrentBiomeInfoClientSide(PlayerEntity entity, Biome biome)
    {
    }

    public Collection<WorldChunk> getLoadedChunks(World world)
    {
        if (world instanceof ServerWorld serverWorld)
        {
            ArrayList<WorldChunk> chunks = new ArrayList<>();

            try
            {
                Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = ((IMixinThreadedAnvilChunkStorage) serverWorld.getChunkManager().threadedAnvilChunkStorage).tellmeGetChunkHolders();

                for (ChunkHolder holder : chunkHolders.values())
                {
                    Optional<WorldChunk> optional = holder.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).left();

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
}
