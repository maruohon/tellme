package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import malilib.util.game.WorldUtils;
import malilib.util.game.wrap.EntityWrap;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.datadump.TileEntityDump;
import fi.dy.masa.tellme.util.BlockInfo;

public class Locate extends ChunkProcessorAllChunks
{
    private final LocateType locateType;
    private final OutputType outputType;
    private final Format format;
    private final Set<String> filters;
    private boolean printDimension;
    private List<LocationData> data = new ArrayList<>();

    private Locate(LocateType locateType, OutputType outputType, Set<String> filters)
    {
        this.locateType = locateType;
        this.outputType = outputType;
        this.filters = filters;
        this.format = outputType == OutputType.DUMP_CSV ? Format.CSV : Format.ASCII;
    }

    public static Locate create(LocateType locateType, OutputType outputType, Set<String> filters)
    {
        return new Locate(locateType, outputType, filters);
    }

    public Locate setPrintDimension(boolean printDimension)
    {
        this.printDimension = printDimension;
        return this;
    }

    public OutputType getOutputType()
    {
        return this.outputType;
    }

    public LocateType getLocateType()
    {
        return this.locateType;
    }

    private IdentityHashMap<IBlockState, Integer> generateBlockStateFilters()
    {
        int i = 0; // dummy
        IdentityHashMap<IBlockState, Integer> blockStateFilter = new IdentityHashMap<IBlockState, Integer>(this.filters.size() * 16);
        ResourceLocation air = new ResourceLocation("minecraft:air");

        for (String str : this.filters)
        {
            int index = str.indexOf('[');
            String name = index > 0 ? str.substring(0, index) : str;
            ResourceLocation key = new ResourceLocation(name);
            Block block = Block.REGISTRY.getObject(key);

            if (block != null && (block != Blocks.AIR || key.equals(air)))
            {
                // First get all valid states for this block
                Collection<IBlockState> states = block.getBlockState().getValidStates();
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

                for (IBlockState state : states)
                {
                    blockStateFilter.put(state, i++);
                }
            }
            else
            {
                TellMe.LOGGER.warn("Invalid block name '{}'", str);
            }
        }

        return blockStateFilter;
    }

    private Set<Class<? extends Entity>> generateEntityFilters()
    {
        Set<Class<? extends Entity>> set = new HashSet<Class<? extends Entity>>();

        for (String name : this.filters)
        {
            Class<? extends Entity> clazz = EntityList.getClassFromName(name);

            if (clazz != null)
            {
                set.add(clazz);
            }
            else
            {
                TellMe.LOGGER.warn("Invalid entity name '{}'", name);
            }
        }

        return set;
    }

    private Set<Class<? extends TileEntity>> generateTileEntityFilters()
    {
        Set<Class<? extends TileEntity>> set = new HashSet<Class<? extends TileEntity>>();

        for (String name : this.filters)
        {
            RegistryNamespaced <ResourceLocation, Class <? extends TileEntity>> registry = TileEntityDump.getTileEntityRegistry();
            Class<? extends TileEntity> clazz = registry.getObject(new ResourceLocation(name));

            if (clazz != null)
            {
                set.add(clazz);
            }
            else
            {
                TellMe.LOGGER.warn("Invalid TileEntity name '{}'", name);
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

    private void locateBlocks(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax, IdentityHashMap<IBlockState, Integer> filters)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        int count = 0;
        final long timeBefore = System.currentTimeMillis();

        for (Chunk chunk : chunks)
        {
            if (this.data.size() >= 100000)
            {
                TellMe.LOGGER.warn("Over 100 000 blocks found already, aborting...");
                break;
            }

            final int dim = WorldUtils.getDimensionId(chunk.getWorld());
            final int topY = chunk.getTopFilledSegment() + 15;
            final int xMin = Math.max(chunk.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunk.z << 4, posMin.getZ());
            final int xMax = Math.min((chunk.x << 4) + 15, posMax.getX());
            final int yMax = Math.min(topY, posMax.getY());
            final int zMax = Math.min((chunk.z << 4) + 15, posMax.getZ());

            for (int z = zMin; z <= zMax; ++z)
            {
                for (int x = xMin; x <= xMax; ++x)
                {
                    for (int y = yMin; y <= yMax; ++y)
                    {
                        pos.setPos(x, y, z);
                        IBlockState state = chunk.getBlockState(pos);

                        if (filters.containsKey(state))
                        {
                            //ResourceLocation name = state.getBlock().getRegistryName();
                            this.data.add(LocationData.of(state.toString(), dim, new Vec3d(x, y, z)));
                            count++;
                        }
                    }
                }
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.LOGGER.info(String.format(Locale.US, "Located %d blocks in %d chunks in %.3f seconds.",
                count, chunks.size(), (timeAfter - timeBefore) / 1000f));
    }

    private void locateEntities(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax, Set<Class<? extends Entity>> filters)
    {
        int count = 0;
        final long timeBefore = System.currentTimeMillis();

        for (Chunk chunk : chunks)
        {
            final int dim = WorldUtils.getDimensionId(chunk.getWorld());
            final int xMin = Math.max(chunk.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunk.z << 4, posMin.getZ());
            final int xMax = Math.min((chunk.x << 4) + 16, posMax.getX());
            final int yMax = Math.min(256, posMax.getY());
            final int zMax = Math.min((chunk.z << 4) + 16, posMax.getZ());
            AxisAlignedBB bb = new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax);

            for (int i = 0; i < chunk.getEntityLists().length; i++)
            {
                ClassInheritanceMultiMap<Entity> map = chunk.getEntityLists()[i];

                for (Entity entity : map)
                {
                    if (filters.contains(entity.getClass()) && entity.getEntityBoundingBox().intersects(bb))
                    {
                        ResourceLocation name = EntityList.getKey(entity);
                        this.data.add(LocationData.of(name.toString(), dim, EntityWrap.getEntityPos(entity)));
                        count++;
                    }
                }
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.LOGGER.info(String.format(Locale.US, "Located %d Entities in %d chunks in %.3f seconds.",
                count, chunks.size(), (timeAfter - timeBefore) / 1000f));
    }

    private void locateTileEntities(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax, Set<Class<? extends TileEntity>> filters)
    {
        int count = 0;
        final long timeBefore = System.currentTimeMillis();
        RegistryNamespaced <ResourceLocation, Class <? extends TileEntity>> registry = TileEntityDump.getTileEntityRegistry();

        for (Chunk chunk : chunks)
        {
            if (this.data.size() >= 100000)
            {
                TellMe.LOGGER.warn("Over 100 000 TileEntities found already, aborting...");
                break;
            }

            final int dim = WorldUtils.getDimensionId(chunk.getWorld());
            final int topY = chunk.getTopFilledSegment() + 15;
            final int xMin = Math.max(chunk.x << 4, posMin.getX());
            final int yMin = Math.max(0, posMin.getY());
            final int zMin = Math.max(chunk.z << 4, posMin.getZ());
            final int xMax = Math.min((chunk.x << 4) + 15, posMax.getX());
            final int yMax = Math.min(topY, posMax.getY());
            final int zMax = Math.min((chunk.z << 4) + 15, posMax.getZ());
            StructureBoundingBox box = StructureBoundingBox.createProper(xMin, yMin, zMin, xMax, yMax, zMax);

            for (TileEntity te : chunk.getTileEntityMap().values())
            {
                BlockPos pos = te.getPos();
                //System.out.printf("plop @ %s - box: %s\n", pos, box);

                if (filters.contains(te.getClass()) && box.isVecInside(pos))
                {
                    ResourceLocation name = registry.getNameForObject(te.getClass());

                    if (name != null)
                    {
                        this.data.add(LocationData.of(name.toString(), dim, new Vec3d(pos)));
                        count++;
                    }
                }
            }
        }

        final long timeAfter = System.currentTimeMillis();
        TellMe.LOGGER.info(String.format(Locale.US, "Located %d TileEntities in %d chunks in %.3f seconds.",
                                         count, chunks.size(), (timeAfter - timeBefore) / 1000f));
    }

    public List<String> getLines()
    {
        DataDump dump = new DataDump(this.printDimension ? 5 : 4, this.format);
        String fmtChunk = this.outputType == OutputType.DUMP_CSV ? "%d,%d" : "%4d,%4d";
        String fmtPos = this.outputType == OutputType.DUMP_CSV ? "x = %.2f, y = %.2f, z = %.2f" : "x = %8.2f, y = %5.2f, z = %8.2f";

        if (this.printDimension)
        {
            for (int i = 0; i < this.data.size(); i++)
            {
                LocationData entry = this.data.get(i);
                Vec3d pos = entry.pos;

                dump.addData(   entry.name,
                                String.valueOf(entry.dim),
                                String.format("r.%d.%d", ((int) pos.x) >> 9, ((int) pos.z) >> 9),
                                String.format(fmtChunk, ((int) pos.x) >> 4, ((int) pos.z) >> 4),
                                String.format(fmtPos, pos.x, pos.y, pos.z));
            }

            dump.addTitle("ID", "Dim", "Region", "Chunk", "Location");
        }
        else
        {
            for (int i = 0; i < this.data.size(); i++)
            {
                LocationData entry = this.data.get(i);
                Vec3d pos = entry.pos;

                dump.addData(   entry.name,
                                String.format("r.%d.%d", ((int) pos.x) >> 9, ((int) pos.z) >> 9),
                                String.format(fmtChunk, ((int) pos.x) >> 4, ((int) pos.z) >> 4),
                                String.format(fmtPos, pos.x, pos.y, pos.z));
            }

            dump.addTitle("ID", "Region", "Chunk", "Location");
        }

        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }

    private static class LocationData
    {
        private final String name;
        private final int dim;
        private final Vec3d pos;

        private LocationData(String name, int dim, Vec3d pos)
        {
            this.name = name;
            this.dim = dim;
            this.pos = pos;
        }

        private static LocationData of(String name, int dim, Vec3d pos)
        {
            return new LocationData(name, dim, pos);
        }
    }

    public enum LocateType
    {
        INVALID,
        BLOCK,
        ENTITY,
        TILE_ENTITY;

        public static LocateType fromArg(String arg)
        {
            switch (arg)
            {
                case "block":   return LocateType.BLOCK;
                case "entity":  return LocateType.ENTITY;
                case "te":      return LocateType.TILE_ENTITY;
                default:        return LocateType.INVALID;
            }
        }
    }

    public enum OutputType
    {
        INVALID,
        DUMP,
        DUMP_CSV,
        PRINT;

        public static OutputType fromArg(String arg)
        {
            switch (arg)
            {
                case "dump":        return OutputType.DUMP;
                case "dump-csv":    return OutputType.DUMP_CSV;
                case "print":       return OutputType.PRINT;
                default:            return OutputType.INVALID;
            }
        }
    }
}
