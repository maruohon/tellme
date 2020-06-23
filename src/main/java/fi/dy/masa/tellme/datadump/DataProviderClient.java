package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.CommandUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class DataProviderClient extends DataProviderBase
{
    //private static final Field field_ClientChunkProvider_array = ObfuscationReflectionHelper.findField(ClientChunkProvider.class, "field_217256_d");
    //private static final Field field_ChunkArray_chunks = ObfuscationReflectionHelper.findField(ClientChunkProvider.ChunkArray.class, "field_217195_b");

    @Override
    public World getWorld(MinecraftServer server, DimensionType dimensionType) throws CommandSyntaxException
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.isSingleplayer())
        {
            return super.getWorld(server, dimensionType);
        }
        else if (mc.world != null && mc.world.getDimension().getType() == dimensionType)
        {
            return mc.world;
        }

        throw CommandUtils.DIMENSION_NOT_LOADED_EXCEPTION.create(dimensionType.getRegistryName().toString());
    }

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
            Vec3d vec = mc.player.getPositionVector();
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
    public void getCurrentBiomeInfoClientSide(Entity entity, Biome biome)
    {
        BlockPos pos = entity.getPosition();
        TextFormatting green = TextFormatting.GREEN;

        // These are client-side only:
        int color = this.getGrassColor(biome, pos);
        entity.sendMessage(new StringTextComponent("Grass color: ")
                    .appendSibling(new StringTextComponent(String.format("0x%08X (%d)", color, color)).applyTextStyle(green)));

        color = this.getFoliageColor(biome, pos);
        entity.sendMessage(new StringTextComponent("Foliage color: ")
                    .appendSibling(new StringTextComponent(String.format("0x%08X (%d)", color, color)).applyTextStyle(green)));
    }

    @Override
    public int getFoliageColor(Biome biome, BlockPos pos)
    {
        return biome.getFoliageColor(pos);
    }

    @Override
    public int getGrassColor(Biome biome, BlockPos pos)
    {
        return biome.getGrassColor(pos);
    }

    @Override
    public String getBiomeName(Biome biome)
    {
        return biome.getDisplayName().getString();
    }

    public void getExtendedBlockStateInfo(World world, BlockState state, BlockPos pos, List<String> lines)
    {
        /*
        try
        {
            state = state.getBlock().getExtendedState(state, world, pos);
        }
        catch (Exception e)
        {
            TellMe.logger.error("getFullBlockInfo(): Exception while calling getExtendedState() on the block");
        }

        if (state instanceof IExtendedBlockState)
        {
            IExtendedBlockState extendedState = (IExtendedBlockState) state;

            if (extendedState.getUnlistedProperties().size() > 0)
            {
                lines.add("IExtendedBlockState properties:");

                UnmodifiableIterator<Entry<IUnlistedProperty<?>, Optional<?>>> iterExt = extendedState.getUnlistedProperties().entrySet().iterator();

                while (iterExt.hasNext())
                {
                    Entry<IUnlistedProperty<?>, Optional<?>> entry = iterExt.next();
                    lines.add(MoreObjects.toStringHelper(entry.getKey())
                            .add("name", entry.getKey().getName())
                            .add("clazz", entry.getKey().getType())
                            .add("value", entry.getValue().toString()).toString());
                }
            }
        }
        */
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
                String name = group.getTabLabel();
                String key = group.getTranslationKey();
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
                names[i++] = I18n.format(group.getTranslationKey());
            }
        }

        names = Arrays.copyOf(names, i);
        obj.add("CreativeTabs", new JsonPrimitive(String.join(",", names)));
    }

    @Override
    public void addMusicTypeData(DataDump dump)
    {
        for (MusicType music : MusicType.values())
        {
            SoundEvent sound = music.getSound();
            String minDelay = String.valueOf(music.getMinDelay());
            String maxDelay = String.valueOf(music.getMaxDelay());
            ResourceLocation regName = sound.getRegistryName();

            dump.addData(music.name().toLowerCase(), regName != null ? regName.toString() : "<null>", minDelay, maxDelay);
        }
    }
}
