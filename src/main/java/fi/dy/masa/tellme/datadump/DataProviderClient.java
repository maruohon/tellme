package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class DataProviderClient extends DataProviderBase
{
    /*
    @Override
    public File getConfigDirectory()
    {
        return new File(MinecraftClient.getInstance().runDirectory, "config");
    }
    */

    @Override
    public Collection<WorldChunk> getLoadedChunks(World world)
    {
        if (world.isClient == false)
        {
            return super.getLoadedChunks(world);
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        if (world instanceof ClientWorld && mc.player != null)
        {
            ArrayList<WorldChunk> list = new ArrayList<>();
            ClientChunkManager chunkManager = ((ClientWorld) world).getChunkManager();
            Vec3d vec = mc.player.getPos();
            ChunkPos center = new ChunkPos(MathHelper.floor(vec.x) >> 4, MathHelper.floor(vec.z) >> 4);
            final int renderDistance = mc.options.viewDistance;

            for (int chunkZ = center.z - renderDistance; chunkZ <= center.z + renderDistance; ++chunkZ)
            {
                for (int chunkX = center.x - renderDistance; chunkX <= center.x + renderDistance; ++chunkX)
                {
                    WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);

                    if (chunk != null)
                    {
                        list.add(chunk);
                    }
                }
            }

            return list;
        }

        return Collections.emptyList();
    }

    @Override
    @Nullable
    public Collection<Advancement> getAdvancements(MinecraftServer server)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.isIntegratedServerRunning() && mc.player != null)
        {
            server = mc.getServer();
            return server != null ? server.getAdvancementLoader().getAdvancements() : null;
        }
        else
        {
            ClientPlayNetworkHandler nh = mc.getNetworkHandler();
            return nh != null ? nh.getAdvancementHandler().getManager().getAdvancements() : null;
        }
    }

    @Override
    public void getCurrentBiomeInfoClientSide(PlayerEntity entity, Biome biome)
    {
        BlockPos pos = entity.getBlockPos();
        String pre = Formatting.GREEN.toString();
        String rst = Formatting.RESET.toString() + Formatting.WHITE.toString();

        // These are client-side only:
        int color = this.getGrassColor(biome, pos);
        entity.sendMessage(new LiteralText(String.format("Grass color: %s0x%08X%s (%s%d%s)", pre, color, rst, pre, color, rst)), false);

        color = this.getFoliageColor(biome, pos);
        entity.sendMessage(new LiteralText(String.format("Foliage color: %s0x%08X%s (%s%d%s)", pre, color, rst, pre, color, rst)), false);
    }

    @Override
    public int getFoliageColor(Biome biome, BlockPos pos)
    {
        return biome.getFoliageColor();
    }

    @Override
    public int getGrassColor(Biome biome, BlockPos pos)
    {
        return biome.getGrassColorAt(pos.getX(), pos.getZ());
    }

    @Override
    public String getBiomeName(Biome biome)
    {
        return biome.getName().getString();
    }

    @Override
    public void addCommandDumpData(DataDump dump, MinecraftServer server)
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
                String name = group.getName();
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

                String translatedName = I18n.translate(key);
                String iconItem = ItemDump.getStackInfoBasic(stack);

                dump.addData(index, name, translatedName, iconItem);
            }
        }
    }

    @Override
    public void addItemGroupNames(JsonObject obj, Item item)
    {
        ItemGroup group = item.getGroup();

        if (group != null)
        {
            String name = I18n.translate(group.getTranslationKey());
            obj.add("CreativeTabs", new JsonPrimitive(name));
        }
    }
}
