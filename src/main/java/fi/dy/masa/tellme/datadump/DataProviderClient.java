package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class DataProviderClient extends DataProviderBase
{
    //private static final Field field_ClientChunkProvider_array = ObfuscationReflectionHelper.findField(ClientChunkProvider.class, "field_217256_d");
    //private static final Field field_ChunkArray_chunks = ObfuscationReflectionHelper.findField(ClientChunkProvider.ChunkArray.class, "field_217195_b");

    @Override
    public Collection<Chunk> getLoadedChunks(World world)
    {
        if (world.isRemote == false)
        {
            return super.getLoadedChunks(world);
        }

        Minecraft mc = Minecraft.getInstance();

        if (world instanceof ClientWorld && mc.player != null)
        {
            ClientChunkProvider provider = ((ClientWorld) world).getChunkProvider();
            Vector3d vec = mc.player.getPositionVec();
            ChunkPos center = new ChunkPos(((int) Math.floor(vec.x)) >> 4, ((int) Math.floor(vec.z)) >> 4);
            ArrayList<Chunk> list = new ArrayList<>();
            final int renderDistance = mc.gameSettings.renderDistanceChunks;

            for (int chunkZ = center.z - renderDistance; chunkZ <= center.z + renderDistance; ++chunkZ)
            {
                for (int chunkX = center.x - renderDistance; chunkX <= center.x + renderDistance; ++chunkX)
                {
                    Chunk chunk = provider.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);

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
    public Collection<Advancement> getAdvacements(@Nullable MinecraftServer server)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.isSingleplayer() && mc.player != null)
        {
            return server != null ? server.getAdvancementManager().getAllAdvancements() : null;
        }
        else
        {
            ClientPlayNetHandler nh = mc.getConnection();

            if (nh != null)
            {
                return nh.getAdvancementManager().getAdvancementList().getAll();
            }
        }

        return null;
    }

    @Override
    public void getCurrentBiomeInfoClientSide(PlayerEntity entity, Biome biome)
    {
        BlockPos pos = entity.getPosition();
        TextFormatting green = TextFormatting.GREEN;

        // These are client-side only:
        int grassColor = biome.getGrassColor(pos.getX(), pos.getZ());
        entity.sendStatusMessage(new StringTextComponent("Grass color: ")
                    .append(new StringTextComponent(String.format("0x%08X (%d)", grassColor, grassColor)).mergeStyle(green)), false);

        int foliageColor = biome.getFoliageColor();
        entity.sendStatusMessage(new StringTextComponent("Foliage color: ")
                    .append(new StringTextComponent(String.format("0x%08X (%d)", foliageColor, foliageColor)).mergeStyle(green)), false);
    }

    @Override
    public int getFoliageColor(Biome biome, BlockPos pos)
    {
        return biome.getFoliageColor();
    }

    @Override
    public int getGrassColor(Biome biome, BlockPos pos)
    {
        return biome.getGrassColor(pos.getX(), pos.getZ());
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
        for (ItemGroup group : ItemGroup.GROUPS)
        {
            if (group != null)
            {
                String index = String.valueOf(group.getIndex());
                String name = group.getPath();
                String key = group.getGroupName().getString();
                ItemStack stack = group.createIcon();

                if (key == null)
                {
                    TellMe.logger.warn("null translation key for tab at index {} (name: '{}')", group.getIndex(), name);
                    continue;
                }

                if (name == null)
                {
                    TellMe.logger.warn("null name for tab at index {} (translation key: '{}')", group.getIndex(), key);
                    continue;
                }

                if (stack == null)
                {
                    TellMe.logger.warn("null icon item for tab at index {} (name: '{}', translation key: '{}')", group.getIndex(), name, key);
                    continue;
                }

                String translatedName = I18n.format(key);
                String iconItem = ItemDump.getStackInfoBasic(stack);

                dump.addData(index, name, translatedName, iconItem);
            }
        }
    }

    @Override
    public void addItemGroupNames(JsonObject obj, Item item)
    {
        String[] names = new String[ItemGroup.GROUPS.length];
        int i = 0;

        for (ItemGroup group : item.getCreativeTabs())
        {
            if (group != null)
            {
                names[i++] = I18n.format(group.getGroupName().getString());
            }
        }

        names = Arrays.copyOf(names, i);
        obj.add("CreativeTabs", new JsonPrimitive(String.join(",", names)));
    }
}
