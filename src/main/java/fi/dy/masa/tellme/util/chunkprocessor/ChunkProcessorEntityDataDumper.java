package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import fi.dy.masa.tellme.TellMe;
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
                ResourceLocation id = new ResourceLocation(str);
                @SuppressWarnings("deprecation")
                Optional<EntityType<?>> type = Registry.ENTITY_TYPE.getOptional(id);
                type.ifPresent(this.filters::add);
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Invalid entity name '{}'", str);
            }
        }
    }

    @Override
    public void processChunk(LevelChunk chunk)
    {
        ClassInstanceMultiMap<Entity>[] entityLists = chunk.getEntitySections();
        Set<EntityType<?>> filters = this.filters;
        boolean noFilters = filters.isEmpty();
        Vec3 min = this.minPos;
        Vec3 max = this.maxPos;
        boolean hasBox = min != null && max != null;
        double minX = min != null ? min.x : 0;
        double minY = min != null ? min.y : 0;
        double minZ = min != null ? min.z : 0;
        double maxX = max != null ? max.x : 0;
        double maxY = max != null ? max.y : 0;
        double maxZ = max != null ? max.z : 0;

        for (ClassInstanceMultiMap<Entity> entityList : entityLists)
        {
            for (Entity entity : entityList)
            {
                Vec3 pos = entity.position();

                if (hasBox &&
                    (pos.x < minX ||
                     pos.y < minY ||
                     pos.z < minZ ||
                     pos.x > maxX ||
                     pos.y > maxY ||
                     pos.z > maxZ))
                {
                    continue;
                }

                EntityType<?> type = entity.getType();

                if (noFilters || filters.contains(type))
                {
                    @SuppressWarnings("deprecation")
                    ResourceLocation id = Registry.ENTITY_TYPE.getKey(type);
                    CompoundTag tag = new CompoundTag();

                    if (entity.saveAsPassenger(tag))
                    {
                        this.data.add(new EntityDataEntry(pos, id.toString(), tag.toString()));
                    }
                }
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
            Vec3 pos = entry.pos;
            dump.addData(String.format("%.2f %.2f %.2f", pos.x, pos.y, pos.z), entry.entityId, entry.nbtData);
        }

        return dump;
    }

    private static class EntityDataEntry
    {
        public final Vec3 pos;
        public final String entityId;
        public final String nbtData;

        public EntityDataEntry(Vec3 pos, String entityId, String nbtData)
        {
            this.pos = pos;
            this.entityId = entityId;
            this.nbtData = nbtData;
        }
    }
}
