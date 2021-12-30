package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.Sets;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class LocateEntities extends LocateBase
{
    protected final Set<EntityType<?>> filters;

    public LocateEntities(DataDump.Format format, List<String> filterStrings) throws CommandSyntaxException
    {
        super(format);

        this.filters = this.generateEntityFilters(filterStrings);
    }

    protected Set<EntityType<?>> generateEntityFilters(List<String> filterStrings) throws CommandSyntaxException
    {
        Set<EntityType<?>> set = Sets.newIdentityHashSet();

        for (String name : filterStrings)
        {
            try
            {
                ResourceLocation key = new ResourceLocation(name);
                @SuppressWarnings("deprecation")
                Optional<EntityType<?>> type = Registry.ENTITY_TYPE.getOptional(key);

                if (type.isPresent())
                {
                    set.add(type.get());
                }
                else
                {
                    TellMe.logger.warn("Invalid entity name '{}'", name);
                    throw INVALID_NAME_EXCEPTION.create(name);
                }
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Invalid entity name '{}'", name);
                throw INVALID_NAME_EXCEPTION.create(name);
            }
        }

        return set;
    }

    @Override
    public void processChunks(Collection<LevelChunk> chunks, BlockPos posMin, BlockPos posMax)
    {
        final long timeBefore = System.currentTimeMillis();
        Set<EntityType<?>> filters = this.filters;
        int count = 0;

        for (LevelChunk chunk : chunks)
        {
            ChunkPos chunkPos = chunk.getPos();
            final String dim = WorldUtils.getDimensionId(chunk.getLevel());
            final int xMin = Math.max(chunkPos.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunkPos.z << 4, posMin.getZ());
            final int xMax = Math.min((chunkPos.x << 4) + 16, posMax.getX());
            final int yMax = Math.min(256, posMax.getY());
            final int zMax = Math.min((chunkPos.z << 4) + 16, posMax.getZ());
            AABB bb = new AABB(xMin, yMin, zMin, xMax, yMax, zMax);

            for (int i = 0; i < chunk.getEntitySections().length; i++)
            {
                ClassInstanceMultiMap<Entity> map = chunk.getEntitySections()[i];

                for (Entity entity : map)
                {
                    EntityType<?> type = entity.getType();

                    if (filters.contains(type) && entity.getBoundingBox().intersects(bb))
                    {
                        String name = EntityInfo.getEntityNameFor(type);
                        this.data.add(LocationData.of(name, dim, entity.position()));
                        count++;
                    }
                }
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.logger.info(String.format(Locale.US, "Located %d Entities in %d chunks in %.3f seconds.",
                                         count, chunks.size(), (timeAfter - timeBefore) / 1000f));
    }
}
