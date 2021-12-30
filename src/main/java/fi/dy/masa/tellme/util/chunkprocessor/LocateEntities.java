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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class LocateEntities extends LocateBase
{
    protected final Set<EntityType<?>> filters;
    protected String dimName = "";
    protected int totalCount;

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
        final long timeBefore = System.nanoTime();

        for (LevelChunk chunk : chunks)
        {
            ChunkPos chunkPos = chunk.getPos();
            this.dimName = WorldUtils.getDimensionId(chunk.getLevel());
            WorldUtils.processEntitiesInChunk(chunk.getLevel(), chunkPos.x, chunkPos.z, this::entityConsumer);
        }

        final long timeAfter = System.nanoTime();
        TellMe.logger.info(String.format(Locale.US, "Located %d Entities in %d chunks in %.3f seconds.",
                                         this.totalCount, chunks.size(), (timeAfter - timeBefore) / 1000000000D));
    }

    private void entityConsumer(Entity entity)
    {
        EntityType<?> type = entity.getType();

        if (this.filters.contains(type))
        {
            String name = EntityInfo.getEntityNameFor(type);
            this.data.add(LocationData.of(name, this.dimName, entity.position()));
            ++this.totalCount;
        }
    }
}
