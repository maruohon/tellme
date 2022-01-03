package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.WorldChunk;
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
                Identifier id = new Identifier(str);
                Optional<BlockEntityType<?>> type = Registry.BLOCK_ENTITY_TYPE.getOrEmpty(id);

                if (type.isPresent())
                {
                    this.filters.add(type.get());
                }
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Invalid block entity name '{}'", str);
            }
        }
    }

    @Override
    public void processChunk(WorldChunk chunk)
    {
        Map<BlockPos, BlockEntity> blockEntities = chunk.getBlockEntities();
        Set<BlockEntityType<?>> filters = this.filters;
        final Vec3d min = this.minPos;
        final Vec3d max = this.maxPos;
        final boolean noFilters = filters.isEmpty();
        final boolean hasBox = min != null && max != null;
        final int minX = hasBox ? MathHelper.floor(min.x) : 0;
        final int minY = hasBox ? MathHelper.floor(min.y) : 0;
        final int minZ = hasBox ? MathHelper.floor(min.z) : 0;
        final int maxX = hasBox ? MathHelper.floor(max.x) : 0;
        final int maxY = hasBox ? MathHelper.floor(max.y) : 0;
        final int maxZ = hasBox ? MathHelper.floor(max.z) : 0;

        for (BlockEntity be : blockEntities.values())
        {
            BlockEntityType<?> type = be.getType();

            if (noFilters || filters.contains(type))
            {
                Identifier id = Registry.BLOCK_ENTITY_TYPE.getId(type);

                if (id != null)
                {
                    BlockPos pos = be.getPos();

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
                        NbtCompound tag = be.createNbt();
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
