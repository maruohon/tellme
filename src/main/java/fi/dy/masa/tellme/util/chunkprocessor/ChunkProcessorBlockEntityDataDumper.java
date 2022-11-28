package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ChunkProcessorBlockEntityDataDumper extends ChunkProcessorBase
{
    private final List<BlockEntityDataEntry> data = new ArrayList<>();
    private final Set<BlockEntityType<?>> filters = new HashSet<>();

    public ChunkProcessorBlockEntityDataDumper(DataDump.Format format, Collection<String> filtersIn)
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
                Optional<BlockEntityType<?>> type = Registry.BLOCK_ENTITY_TYPE.getOptional(id);
                type.ifPresent(this.filters::add);
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Invalid block entity name '{}'", str);
            }
        }
    }

    @Override
    public void processChunk(LevelChunk chunk)
    {
        Map<BlockPos, BlockEntity> blockEntities = chunk.getBlockEntities();
        Set<BlockEntityType<?>> filters = this.filters;
        boolean noFilters = filters.isEmpty();
        Vec3 min = this.minPos;
        Vec3 max = this.maxPos;
        boolean hasBox = min != null && max != null;
        int minX = min != null ? (int) Math.floor(min.x) : 0;
        int minY = min != null ? (int) Math.floor(min.y) : 0;
        int minZ = min != null ? (int) Math.floor(min.z) : 0;
        int maxX = max != null ? (int) Math.floor(max.x) : 0;
        int maxY = max != null ? (int) Math.floor(max.y) : 0;
        int maxZ = max != null ? (int) Math.floor(max.z) : 0;

        for (BlockEntity be : blockEntities.values())
        {
            BlockEntityType<?> type = be.getType();

            if (noFilters || filters.contains(type))
            {
                ResourceLocation id = ForgeRegistries.BLOCK_ENTITIES.getKey(type);

                if (id != null)
                {
                    BlockPos pos = be.getBlockPos();

                    if (hasBox &&
                        (pos.getX() < minX ||
                         pos.getY() < minY ||
                         pos.getZ() < minZ ||
                         pos.getX() > maxX ||
                         pos.getY() > maxY ||
                         pos.getZ() > maxZ))
                    {
                        continue;
                    }

                    try
                    {
                        CompoundTag tag = be.serializeNBT();
                        this.data.add(new BlockEntityDataEntry(pos, id.toString(), tag.toString()));
                    }
                    catch (Exception e)
                    {
                        TellMe.logger.warn("Exception while writing block entity '{}' to NBT", id);
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

        for (BlockEntityDataEntry entry : this.data)
        {
            BlockPos pos = entry.pos;
            dump.addData(String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ()), entry.id, entry.nbtData);
        }

        return dump;
    }

    private static class BlockEntityDataEntry
    {
        public final BlockPos pos;
        public final String id;
        public final String nbtData;

        public BlockEntityDataEntry(BlockPos pos, String id, String nbtData)
        {
            this.pos = pos;
            this.id = id;
            this.nbtData = nbtData;
        }
    }
}
