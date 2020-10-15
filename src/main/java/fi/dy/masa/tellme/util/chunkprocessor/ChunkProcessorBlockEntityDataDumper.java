package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ChunkProcessorBlockEntityDataDumper extends ChunkProcessorBase
{
    private final List<BlockEntityDataEntry> data = new ArrayList<>();
    private final Set<TileEntityType<?>> filters = new HashSet<>();

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
                Optional<TileEntityType<?>> type = Registry.BLOCK_ENTITY_TYPE.getOptional(id);

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
    public void processChunk(Chunk chunk)
    {
        Map<BlockPos, TileEntity> blockEntities = chunk.getTileEntityMap();
        Set<TileEntityType<?>> filters = this.filters;
        boolean noFilters = filters.isEmpty();
        Vector3d min = this.minPos;
        Vector3d max = this.maxPos;
        boolean hasBox = min != null && max != null;
        int minX = min != null ? (int) Math.floor(min.x) : 0;
        int minY = min != null ? (int) Math.floor(min.y) : 0;
        int minZ = min != null ? (int) Math.floor(min.z) : 0;
        int maxX = max != null ? (int) Math.floor(max.x) : 0;
        int maxY = max != null ? (int) Math.floor(max.y) : 0;
        int maxZ = max != null ? (int) Math.floor(max.z) : 0;
        int total = 0;

        for (TileEntity be : blockEntities.values())
        {
            TileEntityType<?> type = be.getType();

            if (noFilters || filters.contains(type))
            {
                ResourceLocation id = ForgeRegistries.TILE_ENTITIES.getKey(type);

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
                        CompoundNBT tag = be.write(new CompoundNBT());
                        this.data.add(new BlockEntityDataEntry(pos, id.toString(), tag.toString()));
                        ++total;
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
