package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class DataProviderClient extends DataProviderBase
{
    //private static final Field field_ClientChunkProvider_array = ObfuscationReflectionHelper.findField(ClientChunkProvider.class, "storage");
    //private static final Field field_ChunkArray_chunks = ObfuscationReflectionHelper.findField(ClientChunkProvider.ChunkArray.class, "chunks");

    @Override
    public Collection<LevelChunk> getLoadedChunks(Level world)
    {
        if (world.isClientSide == false)
        {
            return super.getLoadedChunks(world);
        }

        Minecraft mc = Minecraft.getInstance();

        if (world instanceof ClientLevel && mc.player != null)
        {
            ClientChunkCache provider = ((ClientLevel) world).getChunkSource();
            Vec3 vec = mc.player.position();
            ChunkPos center = new ChunkPos(((int) Math.floor(vec.x)) >> 4, ((int) Math.floor(vec.z)) >> 4);
            ArrayList<LevelChunk> list = new ArrayList<>();
            final int renderDistance = mc.options.getEffectiveRenderDistance();

            for (int chunkZ = center.z - renderDistance; chunkZ <= center.z + renderDistance; ++chunkZ)
            {
                for (int chunkX = center.x - renderDistance; chunkX <= center.x + renderDistance; ++chunkX)
                {
                    LevelChunk chunk = provider.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);

                    if (chunk != null)
                    {
                        list.add(chunk);
                    }
                }
            }

            /*
            try
            {
                Object array = field_ClientChunkProvider_array.get(provider);
                @SuppressWarnings("unchecked")
                AtomicReferenceArray<Chunk> chunks = (AtomicReferenceArray<Chunk>) field_ChunkArray_chunks.get(array);
                final int size = chunks.length();

                for (int i = 0; i < size; ++i)
                {
                    list.add(chunks.get(i));
                }
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Failed to get the loaded chunks on the client", e);
            }
            */

            return list;
        }

        return Collections.emptyList();
    }

    @Override
    @Nullable
    public Collection<Advancement> getAdvancements(@Nullable MinecraftServer server)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.hasSingleplayerServer() && mc.player != null)
        {
            return server != null ? server.getAdvancements().getAllAdvancements() : null;
        }
        else
        {
            ClientPacketListener nh = mc.getConnection();

            if (nh != null)
            {
                return nh.getAdvancements().getAdvancements().getAllAdvancements();
            }
        }

        return null;
    }

    @Override
    public void getCurrentBiomeInfoClientSide(Player entity, Biome biome)
    {
        BlockPos pos = entity.blockPosition();
        ChatFormatting green = ChatFormatting.GREEN;

        // These are client-side only:
        int grassColor = biome.getGrassColor(pos.getX(), pos.getZ());
        entity.displayClientMessage(Component.literal("Grass color: ")
                    .append(Component.literal(String.format("0x%08X (%d)", grassColor, grassColor)).withStyle(green)), false);

        int foliageColor = biome.getFoliageColor();
        entity.displayClientMessage(Component.literal("Foliage color: ")
                    .append(Component.literal(String.format("0x%08X (%d)", foliageColor, foliageColor)).withStyle(green)), false);
    }

    @Override
    public void addCommandDumpData(DataDump dump, @Nullable MinecraftServer server)
    {
        // TODO 1.14
        super.addCommandDumpData(dump, server);
    }

    @Override
    public void addItemGroupData(DataDump dump)
    {
        for (CreativeModeTab group : CreativeModeTab.TABS)
        {
            if (group != null)
            {
                String index = String.valueOf(group.getId());
                String name = group.getRecipeFolderName();
                String key = group.getDisplayName().getString();
                ItemStack stack = group.makeIcon();

                if (key == null)
                {
                    TellMe.logger.warn("null translation key for tab at index {} (name: '{}')", group.getId(), name);
                    continue;
                }

                if (name == null)
                {
                    TellMe.logger.warn("null name for tab at index {} (translation key: '{}')", group.getId(), key);
                    continue;
                }

                if (stack == null)
                {
                    TellMe.logger.warn("null icon item for tab at index {} (name: '{}', translation key: '{}')", group.getId(), name, key);
                    continue;
                }

                String translatedName = I18n.get(key);
                String iconItem = ItemDump.getStackInfoBasic(stack);

                dump.addData(index, name, translatedName, iconItem);
            }
        }
    }

    @Override
    public void addItemGroupNames(JsonObject obj, Item item)
    {
        String[] names = new String[CreativeModeTab.TABS.length];
        int i = 0;

        for (CreativeModeTab group : item.getCreativeTabs())
        {
            if (group != null)
            {
                names[i++] = I18n.get(group.getDisplayName().getString());
            }
        }

        names = Arrays.copyOf(names, i);
        obj.add("CreativeTabs", new JsonPrimitive(String.join(",", names)));
    }
}
