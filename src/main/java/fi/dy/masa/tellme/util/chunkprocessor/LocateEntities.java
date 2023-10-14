package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.Sets;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.mixin.IMixinWorld;
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
                Identifier key = new Identifier(name);
                Optional<EntityType<?>> type = Registries.ENTITY_TYPE.getOrEmpty(key);

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
    public void processChunks(Collection<WorldChunk> chunks, BlockPos posMin, BlockPos posMax)
    {
        final long timeBefore = System.nanoTime();

        for (WorldChunk chunk : chunks)
        {
            ChunkPos chunkPos = chunk.getPos();

            final int xMin = Math.max(chunkPos.x << 4, posMin.getX());
            final int zMin = Math.max(chunkPos.z << 4, posMin.getZ());
            final int xMax = Math.min((chunkPos.x << 4) + 16, posMax.getX());
            final int zMax = Math.min((chunkPos.z << 4) + 16, posMax.getZ());
            //final int yMax = Math.min(chunk.getTopY(), posMax.getY());
            //final int yMin = Math.max(chunk.getBottomY(), posMin.getY());

            this.dimName = WorldUtils.getDimensionId(chunk.getWorld());
            Box bb = new Box(xMin, Double.MIN_VALUE, zMin, xMax, Double.MAX_VALUE, zMax);
            World world = chunk.getWorld();
            ((IMixinWorld) world).tellme_getEntityLookup().forEachIntersects(bb, this::entityConsumer);
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
            this.data.add(LocationData.of(name, this.dimName, entity.getPos()));
            ++this.totalCount;
        }
    }
}
