package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.mixin.IMixinWorld;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ChunkProcessorEntityDataDumper extends ChunkProcessorBase
{
    private final List<EntityDataEntry> data = new ArrayList<>();
    private final Set<EntityType<?>> filters = new HashSet<>();

    public ChunkProcessorEntityDataDumper(DataDump.Format format, Collection<String> filtersIn)
    {
        super(format);

        this.setFilters(filtersIn);
    }

    private void setFilters(Collection<String> filtersIn)
    {
        this.filters.clear();
        
        for (String str : filtersIn)
        {
            try
            {
                Identifier id = new Identifier(str);
                Optional<EntityType<?>> type = Registries.ENTITY_TYPE.getOrEmpty(id);

                if (type.isPresent())
                {
                    this.filters.add(type.get());
                }
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Invalid entity name '{}'", str);
            }
        }
    }

    @Override
    public void processChunk(WorldChunk chunk)
    {
        World world = chunk.getWorld();
        ChunkPos pos = chunk.getPos();
        Box box = WorldUtils.createEntityBoxForChunk(world, pos.x, pos.z);
        ((IMixinWorld) world).tellme_getEntityLookup().forEachIntersects(box, this::entityConsumer);
    }

    private void entityConsumer(Entity entity)
    {
        Vec3d min = this.minPos;
        Vec3d max = this.maxPos;
        Vec3d pos = entity.getPos();

        if (min != null && max != null &&
            (pos.x < min.x ||
             pos.y < min.y ||
             pos.z < min.z ||
             pos.x > max.x ||
             pos.y > max.y ||
             pos.z > max.z))
        {
            return;
        }

        EntityType<?> type = entity.getType();

        if (this.filters.isEmpty() || this.filters.contains(type))
        {
            Identifier id = Registries.ENTITY_TYPE.getId(type);
            NbtCompound tag = new NbtCompound();

            if (entity.saveSelfNbt(tag))
            {
                this.data.add(new EntityDataEntry(pos, id.toString(), tag.toString()));
            }
        }
    }

    @Override
    public DataDump getDump()
    {
        DataDump dump = new DataDump(3, this.format);

        dump.setSort(false);
        dump.addTitle("Position", "ID", "NBT Data");

        for (EntityDataEntry entry : this.data)
        {
            Vec3d pos = entry.pos;
            dump.addData(String.format("%.2f %.2f %.2f", pos.x, pos.y, pos.z), entry.entityId, entry.nbtData);
        }

        return dump;
    }

    private static class EntityDataEntry
    {
        public final Vec3d pos;
        public final String entityId;
        public final String nbtData;

        public EntityDataEntry(Vec3d pos, String entityId, String nbtData)
        {
            this.pos = pos;
            this.entityId = entityId;
            this.nbtData = nbtData;
        }
    }
}
