package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.List;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import fi.dy.masa.tellme.mixin.IMixinThreadedAnvilChunkStorage;
import fi.dy.masa.tellme.mixin.IMixinWorld;

public class WorldUtils
{
    public static int getLoadedChunkCount(ServerWorld world)
    {
        //return ((ServerChunkManager) world.getChunkManager()).getTotalChunksLoadedCount();
        Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = ((IMixinThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage).tellme_getChunkHolders();
        return chunkHolders.size();
    }

    public static List<WorldChunk> loadAndGetChunks(World world, ChunkPos posMin, ChunkPos posMax)
    {
        List<WorldChunk> chunks = new ArrayList<>();

        for (int cZ = posMin.z; cZ <= posMax.z; cZ++)
        {
            for (int cX = posMin.x; cX <= posMax.x; cX++)
            {
                chunks.add(world.getChunk(cX, cZ));
            }
        }

        return chunks;
    }

    public static String getDimensionId(World world)
    {
        Identifier id = world.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).getId(world.getDimension());
        return id != null ? id.toString() : "?";
    }

    public static Box createEntityBoxForChunk(World world, int chunkX, int chunkZ)
    {
        double minY = world.getBottomY();
        double maxY = world.getTopY();
        double minX = chunkX * 16.0;
        double minZ = chunkZ * 16.0;
        return new Box(minX       , minY, minZ,
                       minX + 16.0, maxY, minZ + 16.0);
    }

    public static int getEntityCountInChunk(World world, int chunkX, int chunkZ)
    {
        Box box = createEntityBoxForChunk(world, chunkX, chunkZ);
        SimpleEntityCounter counter = new SimpleEntityCounter();
        ((IMixinWorld) world).tellme_getEntityLookup().forEachIntersects(box, counter::countEntity);

        return counter.getCount();
    }

    public static int countEntitiesInChunk(World world, int chunkX, int chunkZ,
                                           Object2IntOpenHashMap<EntityType<?>> perTypeCount)
    {
        Box box = createEntityBoxForChunk(world, chunkX, chunkZ);
        PerTypeEntityCounter counter = new PerTypeEntityCounter(perTypeCount);
        ((IMixinWorld) world).tellme_getEntityLookup().forEachIntersects(box, counter::countEntity);

        return counter.getCount();
    }

    public static class SimpleEntityCounter
    {
        private int count;

        public void countEntity(Entity entity)
        {
            ++this.count;
        }

        public int getCount()
        {
            return this.count;
        }
    }

    public static class PerTypeEntityCounter
    {
        private final Object2IntOpenHashMap<EntityType<?>> perTypeCount;
        private int count;

        public PerTypeEntityCounter(Object2IntOpenHashMap<EntityType<?>> perTypeCount)
        {
            this.perTypeCount = perTypeCount;
        }

        public void countEntity(Entity entity)
        {
            ++this.count;
            this.perTypeCount.addTo(entity.getType(), 1);
        }

        public int getCount()
        {
            return this.count;
        }
    }
}
