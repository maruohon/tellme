package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class Locate extends ChunkProcessorAllChunks
{
    private static final String FMT_COORD_2 = "%.2f";
    private static final String FMT_REGION = "r.%d.%d";
    private static final String FMT_CHUNK = "%d, %d";
    private static final String FMT_CHUNK_5 = "%5d, %5d";
    private static final String FMT_COORDS = "x = %.2f, y = %.2f, z = %.2f";
    private static final String FMT_COORDS_8 = "x = %8.2f, y = %5.2f, z = %8.2f";

    private final List<LocationData> data = new ArrayList<>();
    private final LocateType locateType;
    private final DataDump.Format format;
    private final Collection<String> filters;
    private boolean printDimension;

    private Locate(LocateType locateType, DataDump.Format format, Collection<String> filters)
    {
        this.locateType = locateType;
        this.filters = filters;
        this.format = format;
    }

    public static Locate create(LocateType locateType, DataDump.Format format, Collection<String> filters)
    {
        return new Locate(locateType, format, filters);
    }

    public Locate setPrintDimension(boolean printDimension)
    {
        this.printDimension = printDimension;
        return this;
    }

    public LocateType getLocateType()
    {
        return this.locateType;
    }

    private Set<BlockState> generateBlockStateFilters()
    {
        Set<BlockState> filters = Sets.newIdentityHashSet();
        ResourceLocation air = new ResourceLocation("minecraft:air");

        for (String str : this.filters)
        {
            int index = str.indexOf('[');
            String name = index > 0 ? str.substring(0, index) : str;
            ResourceLocation key = new ResourceLocation(name);
            Block block = ForgeRegistries.BLOCKS.getValue(key);

            if (block != null && (block != Blocks.AIR || key.equals(air)))
            {
                // First get all valid states for this block
                Collection<BlockState> states = block.getStateContainer().getValidStates();
                // Then get the list of properties and their values in the given name (if any)
                List<Pair<String, String>> props = BlockInfo.getProperties(str);

                // ... and then filter the list of all valid states by the provided properties and their values
                if (props.isEmpty() == false)
                {
                    for (Pair<String, String> pair : props)
                    {
                        states = BlockInfo.getFilteredStates(states, pair.getLeft(), pair.getRight());
                    }
                }

                for (BlockState state : states)
                {
                    filters.add(state);
                }
            }
            else
            {
                TellMe.logger.warn("Invalid block name '{}'", str);
            }
        }

        return filters;
    }

    private Set<EntityType<?>> generateEntityFilters()
    {
        Set<EntityType<?>> set = Sets.newIdentityHashSet();

        for (String name : this.filters)
        {
            try
            {
                ResourceLocation key = new ResourceLocation(name);

                if (ForgeRegistries.ENTITIES.containsKey(key))
                {
                    EntityType<?> type = ForgeRegistries.ENTITIES.getValue(key);

                    if (type != null)
                    {
                        set.add(type);
                    }
                }
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Invalid entity name '{}'", name);
            }
        }

        return set;
    }

    private Set<TileEntityType<?>> generateTileEntityFilters()
    {
        Set<TileEntityType<?>> set = Sets.newIdentityHashSet();

        for (String name : this.filters)
        {
            try
            {
                TileEntityType<?> type = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(name));

                if (type != null)
                {
                    set.add(type);
                }
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Invalid TileEntity name '{}'", name);
            }
        }

        return set;
    }

    @Override
    public void processChunks(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax)
    {
        switch (this.locateType)
        {
            case BLOCK:
                this.locateBlocks(chunks, posMin, posMax, this.generateBlockStateFilters());
                break;
            case ENTITY:
                this.locateEntities(chunks, posMin, posMax, this.generateEntityFilters());
                break;
            case TILE_ENTITY:
                this.locateTileEntities(chunks, posMin, posMax, this.generateTileEntityFilters());
                break;
            default:
        }
    }

    private void locateBlocks(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax, Set<BlockState> filters)
    {
        final long timeBefore = System.currentTimeMillis();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        int count = 0;

        for (Chunk chunk : chunks)
        {
            if (this.data.size() > 100000)
            {
                TellMe.logger.warn("Over 100 000 blocks found already, aborting...");
                break;
            }

            ChunkPos chunkPos = chunk.getPos();
            final String dim = WorldUtils.getDimensionId(chunk.getWorld());
            final int topY = chunk.getTopFilledSegment() + 15;
            final int xMin = Math.max(chunkPos.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunkPos.z << 4, posMin.getZ());
            final int xMax = Math.min((chunkPos.x << 4) + 15, posMax.getX());
            final int yMax = Math.min(topY, posMax.getY());
            final int zMax = Math.min((chunkPos.z << 4) + 15, posMax.getZ());

            for (int z = zMin; z <= zMax; ++z)
            {
                for (int x = xMin; x <= xMax; ++x)
                {
                    for (int y = yMin; y <= yMax; ++y)
                    {
                        pos.setPos(x, y, z);
                        BlockState state = chunk.getBlockState(pos);

                        if (filters.contains(state))
                        {
                            ResourceLocation name = state.getBlock().getRegistryName();
                            this.data.add(LocationData.of(name.toString(), dim, new Vec3d(x, y, z)));
                            count++;
                        }
                    }
                }
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.logger.info(String.format(Locale.US, "Located %d blocks in %d chunks in %.3f seconds.",
                count, chunks.size(), (timeAfter - timeBefore) / 1000f));
    }

    private void locateEntities(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax, Set<EntityType<?>> filters)
    {
        int count = 0;
        final long timeBefore = System.currentTimeMillis();

        for (Chunk chunk : chunks)
        {
            ChunkPos chunkPos = chunk.getPos();
            final String dim = WorldUtils.getDimensionId(chunk.getWorld());
            final int xMin = Math.max(chunkPos.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunkPos.z << 4, posMin.getZ());
            final int xMax = Math.min((chunkPos.x << 4) + 16, posMax.getX());
            final int yMax = Math.min(256, posMax.getY());
            final int zMax = Math.min((chunkPos.z << 4) + 16, posMax.getZ());
            AxisAlignedBB bb = new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax);

            for (int i = 0; i < chunk.getEntityLists().length; i++)
            {
                ClassInheritanceMultiMap<Entity> map = chunk.getEntityLists()[i];

                for (Entity entity : map)
                {
                    EntityType<?> type = entity.getType();

                    if (filters.contains(type) && entity.getBoundingBox().intersects(bb))
                    {
                        String name = EntityInfo.getEntityNameFor(type);
                        this.data.add(LocationData.of(name, dim, entity.getPositionVector()));
                        count++;
                    }
                }
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.logger.info(String.format(Locale.US, "Located %d Entities in %d chunks in %.3f seconds.",
                count, chunks.size(), (timeAfter - timeBefore) / 1000f));
    }

    private void locateTileEntities(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax, Set<TileEntityType<?>> filters)
    {
        int count = 0;
        final long timeBefore = System.currentTimeMillis();

        for (Chunk chunk : chunks)
        {
            if (this.data.size() >= 100000)
            {
                TellMe.logger.warn("Over 100 000 TileEntities found already, aborting...");
                break;
            }

            ChunkPos chunkPos = chunk.getPos();
            final String dim = WorldUtils.getDimensionId(chunk.getWorld());
            final int topY = chunk.getTopFilledSegment() + 15;
            final int xMin = Math.max(chunkPos.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunkPos.z << 4, posMin.getZ());
            final int xMax = Math.min((chunkPos.x << 4) + 15, posMax.getX());
            final int yMax = Math.min(topY, posMax.getY());
            final int zMax = Math.min((chunkPos.z << 4) + 15, posMax.getZ());
            MutableBoundingBox box = MutableBoundingBox.createProper(xMin, yMin, zMin, xMax, yMax, zMax);

            for (TileEntity te : chunk.getTileEntityMap().values())
            {
                BlockPos pos = te.getPos();
                TileEntityType<?> type = te.getType();
                //System.out.printf("plop @ %s - box: %s\n", pos, box);

                if (filters.contains(type) && box.isVecInside(pos))
                {
                    String name = BlockInfo.getBlockEntityNameFor(type);
                    this.data.add(LocationData.of(name.toString(), dim, new Vec3d(pos)));
                    count++;
                }
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.logger.info(String.format(Locale.US, "Located %d TileEntities in %d chunks in %.3f seconds.",
                count, chunks.size(), (timeAfter - timeBefore) / 1000f));
    }

    public List<String> getLines()
    {
        int columnCount = this.format == Format.CSV ? 8 : 4;

        if (this.printDimension)
        {
            columnCount += 1;
        }

        DataDump dump = new DataDump(columnCount, this.format);

        for (int i = 0; i < this.data.size(); i++)
        {
            LocationData entry = this.data.get(i);
            this.addLine(dump, entry, this.printDimension, this.format);
        }

        if (this.format == Format.CSV)
        {
            if (this.printDimension)
            {
                dump.addTitle("ID", "Dim", "RX", "RZ", "CX", "CZ", "x", "y", "z");
            }
            else
            {
                dump.addTitle("ID", "RX", "RZ", "CX", "CZ", "x", "y", "z");
            }
        }
        else
        {
            if (this.printDimension)
            {
                dump.addTitle("ID", "Dim", "Region", "Chunk", "Location");
            }
            else
            {
                dump.addTitle("ID", "Region", "Chunk", "Location");
            }
        }

        return dump.getLines();
    }

    private void addLine(DataDump dump, LocationData data, boolean addDimension, Format format)
    {
        Vec3d pos = data.pos;
        int rx = ((int) pos.x) >> 9;
        int rz = ((int) pos.z) >> 9;
        int cx = ((int) pos.x) >> 4;
        int cz = ((int) pos.z) >> 4;

        if (format == Format.CSV)
        {
            String fmtCoord = FMT_COORD_2;

            if (addDimension)
            {
                dump.addData(data.name,
                             data.dimension,
                             String.valueOf(rx), String.valueOf(rz),
                             String.valueOf(cx), String.valueOf(cz),
                             String.format(fmtCoord, pos.x), String.format(fmtCoord, pos.y), String.format(fmtCoord, pos.z));
            }
            else
            {
                dump.addData(data.name,
                             String.valueOf(rx), String.valueOf(rz),
                             String.valueOf(cx), String.valueOf(cz),
                             String.format(fmtCoord, pos.x), String.format(fmtCoord, pos.y), String.format(fmtCoord, pos.z));
            }
        }
        else
        {
            String fmtRegion = FMT_REGION;
            String fmtChunk = format == Format.ASCII ? FMT_CHUNK_5 : FMT_CHUNK;
            String fmtPos = format == Format.ASCII ? FMT_COORDS_8 : FMT_COORDS;

            if (addDimension)
            {
                dump.addData(data.name,
                             data.dimension,
                             String.format(fmtRegion, rx, rz),
                             String.format(fmtChunk, cx, cz),
                             String.format(fmtPos, pos.x, pos.y, pos.z));
            }
            else
            {
                dump.addData(data.name,
                             String.format(fmtRegion, rx, rz),
                             String.format(fmtChunk, cx, cz),
                             String.format(fmtPos, pos.x, pos.y, pos.z));
            }
        }
    }

    private static class LocationData
    {
        private final String name;
        private final String dimension;
        private final Vec3d pos;

        private LocationData(String name, String dimension, Vec3d pos)
        {
            this.name = name;
            this.dimension = dimension;
            this.pos = pos;
        }

        private static LocationData of(String name, String dimension, Vec3d pos)
        {
            return new LocationData(name, dimension, pos);
        }
    }

    public enum LocateType
    {
        BLOCK       ("block",       "blocks",           () -> ForgeRegistries.BLOCKS),
        ENTITY      ("entity",      "entities",         () -> ForgeRegistries.ENTITIES),
        TILE_ENTITY ("tile-entity", "tile_entities",    () -> ForgeRegistries.TILE_ENTITIES);

        private final String argument;
        private final String plural;
        private final Supplier<IForgeRegistry<?>> registrySupplier;

        LocateType(String argument, String plural, Supplier<IForgeRegistry<?>> registrySupplier)
        {
            this.argument = argument;
            this.plural = plural;
            this.registrySupplier = registrySupplier;
        }

        public String getArgument()
        {
            return this.argument;
        }

        public String getPlural()
        {
            return this.plural;
        }

        public Supplier<IForgeRegistry<?>> getRegistrySupplier()
        {
            return this.registrySupplier;
        }
    }
}
