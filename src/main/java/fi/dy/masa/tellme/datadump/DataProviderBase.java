package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

public class DataProviderBase
{
    private static final Field field_ChunkManager_immutableLoadedChunks = ObfuscationReflectionHelper.findField(ChunkMap.class, "f_140130_");

    @Nullable
    public Collection<Advancement> getAdvancements(@Nullable MinecraftServer server)
    {
        return server != null ? server.getAdvancements().getAllAdvancements() : null;
    }

    public void getCurrentBiomeInfoClientSide(Player entity, Biome biome)
    {
    }

    public Collection<LevelChunk> getLoadedChunks(Level world)
    {
        if (world instanceof ServerLevel)
        {
            ArrayList<LevelChunk> chunks = new ArrayList<>();

            try
            {
                @SuppressWarnings("unchecked")
                Long2ObjectLinkedOpenHashMap<ChunkHolder> immutableLoadedChunks = (Long2ObjectLinkedOpenHashMap<ChunkHolder>) field_ChunkManager_immutableLoadedChunks.get(((ServerLevel) world).getChunkSource().chunkMap);

                for (ChunkHolder holder : immutableLoadedChunks.values())
                {
                    Optional<LevelChunk> optional = holder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();

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

    public void addCommandDumpData(DataDump dump, @Nullable MinecraftServer server)
    {
        if (server != null)
        {
            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();

            for (CommandNode<CommandSourceStack> cmd : dispatcher.getRoot().getChildren())
            {
                String cmdName = cmd.getName();
                Command<CommandSourceStack> command = cmd.getCommand();
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
