package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class WorldUtils
{
    public static int getLoadedChunkCount(Level world)
    {
        if (world != null && world.getChunkSource() instanceof ServerChunkCache)
        {
            return ((ServerChunkCache) world.getChunkSource()).chunkMap.getTickingGenerated();
        }

        return 0;
    }

    public static List<LevelChunk> loadAndGetChunks(Level world, ChunkPos posMin, ChunkPos posMax)
    {
        List<LevelChunk> chunks = new ArrayList<>();

        for (int cZ = posMin.z; cZ <= posMax.z; cZ++)
        {
            for (int cX = posMin.x; cX <= posMax.x; cX++)
            {
                chunks.add(world.getChunk(cX, cZ));
            }
        }

        return chunks;
    }

    public static String getDimensionId(Level world)
    {
        Optional<? extends Registry<DimensionType>> opt = world.registryAccess().registry(Registry.DIMENSION_TYPE_REGISTRY);
        ResourceLocation id = opt.isPresent() ? opt.get().getKey(world.dimensionType()) : null;
        return id != null ? id.toString() : "?";
    }

    public static AABB createEntityBoxForChunk(Level world, int chunkX, int chunkZ)
    {
        double minY = world.getMinBuildHeight();
        double maxY = world.getMaxBuildHeight();
        double minX = chunkX * 16.0;
        double minZ = chunkZ * 16.0;
        return new AABB(minX       , minY, minZ,
                        minX + 16.0, maxY, minZ + 16.0);
    }

    public static int getEntityCountInChunk(Level world, int chunkX, int chunkZ)
    {
        AABB box = createEntityBoxForChunk(world, chunkX, chunkZ);
        return world.getEntities((Entity) null, box, (e) -> box.contains(e.position())).size();
    }

    public static int countEntitiesInChunk(Level world, int chunkX, int chunkZ,
                                           Object2IntOpenHashMap<EntityType<?>> perTypeCount)
    {
        AABB box = createEntityBoxForChunk(world, chunkX, chunkZ);
        PerTypeEntityCounter counter = new PerTypeEntityCounter(perTypeCount);

        for (Entity entity : world.getEntities((Entity) null, box, (e) -> box.contains(e.position())))
        {
            counter.countEntity(entity);
        }

        return counter.getCount();
    }

    public static void processEntitiesInChunk(Level world, int chunkX, int chunkZ, Consumer<Entity> consumer)
    {
        AABB box = createEntityBoxForChunk(world, chunkX, chunkZ);

        for (Entity entity : world.getEntities((Entity) null, box, (e) -> box.contains(e.position())))
        {
            consumer.accept(entity);
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
