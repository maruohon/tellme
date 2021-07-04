package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.tellme.util.chunkprocessor.ChunkProcessorLoadedChunks;
import fi.dy.masa.tellme.util.chunkprocessor.EntitiesLister;
import fi.dy.masa.tellme.util.chunkprocessor.EntitiesPerChunkCounter;
import fi.dy.masa.tellme.util.chunkprocessor.EntitiesPerTypeCounter;
import fi.dy.masa.tellme.util.chunkprocessor.TileEntitiesLister;
import fi.dy.masa.tellme.util.chunkprocessor.TileEntitiesPerChunkCounter;
import fi.dy.masa.tellme.util.chunkprocessor.TileEntitiesPerTypeCounter;

public class EntityCountDump extends DataDump
{
    public EntityCountDump(int columns)
    {
        super(columns);

        this.setSort(false);
        this.setRepeatTitleAtBottom(false);
        this.setUseColumnSeparator(true);
    }

    private static ChunkProcessorLoadedChunks createChunkProcessor(EntityListType type)
    {
        switch (type)
        {
            case ALL_ENTITIES:              return new EntitiesLister();
            case ENTITIES_BY_TYPE:          return new EntitiesPerTypeCounter();
            case ENTITIES_BY_CHUNK:         return new EntitiesPerChunkCounter();
            case ALL_TILE_ENTITIES:         return new TileEntitiesLister();
            case TILE_ENTITIES_BY_TYPE:     return new TileEntitiesPerTypeCounter();
            case TILE_ENTITIES_BY_CHUNK:    return new TileEntitiesPerChunkCounter();
        }

        return null;
    }

    public static List<String> getFormattedEntityCountDumpAll(World world, EntityListType type)
    {
        ChunkProcessorLoadedChunks processor = createChunkProcessor(type);

        processor.processAllLoadedChunks(world);

        EntityCountDump dump = processor.createDump(world);
        //final int loadedChunks = WorldUtils.getLoadedChunkCount(world);
        final int loadedChunks = processor.getLoadedChunkCount();
        final int zeroCount = processor.getChunksWithZeroCount();

        dump.addHeader(0, String.format("World '%s' (dim: %s)", world.provider.getDimensionType().getName(), WorldUtils.getDimensionAsString(world)));
        dump.addHeader(1, String.format("Loaded chunks: %d", loadedChunks));

        if (zeroCount != 0)
        {
            dump.addFooter(String.format("Out of %d loaded chunks in total,", loadedChunks));
            dump.addFooter(String.format("there were %d chunks with no %s.", zeroCount, type.getTypeNamePlural()));
        }

        return dump.getLines();
    }

    public static List<String> getFormattedEntityCountDumpArea(World world, EntityListType type, ChunkPos pos1In, ChunkPos pos2In)
    {
        ChunkPos pos1 = new ChunkPos(Math.min(pos1In.x, pos2In.x), Math.min(pos1In.z, pos2In.z));
        ChunkPos pos2 = new ChunkPos(Math.max(pos1In.x, pos2In.x), Math.max(pos1In.z, pos2In.z));
        ChunkProcessorLoadedChunks processor = createChunkProcessor(type);

        processor.processChunksInArea(world,pos1, pos2);

        EntityCountDump dump = processor.createDump(world);
        final int loadedChunks = processor.getLoadedChunkCount();
        final int unloadedChunks = processor.getUnloadedChunkCount();
        final int zeroCount = processor.getChunksWithZeroCount();

        dump.addHeader(0, String.format("World '%s' (dim: %s)", world.provider.getDimensionType().getName(), WorldUtils.getDimensionAsString(world)));
        dump.addHeader(1, String.format("The selected area contains %d loaded chunks", loadedChunks));

        if (pos1.equals(pos2))
        {
            dump.addHeader(2, String.format("Chunk: [%d, %d]", pos1.x, pos1.z));
        }
        else
        {
            dump.addHeader(2, String.format("Chunks: [%d, %d] to [%d, %d]", pos1.x, pos1.z, pos2.x, pos2.z));
        }

        if (zeroCount != 0)
        {
            dump.addFooter(String.format("Out of %d loaded chunks in total in the selected area,", loadedChunks));
            dump.addFooter(String.format("there were %d chunks with no %s.", zeroCount, type.getTypeNamePlural()));
        }

        if (unloadedChunks != 0)
        {
            dump.addFooter(String.format("There were also %d unloaded chunks in the selected area.", unloadedChunks));
        }

        return dump.getLines();
    }

    public enum EntityListType
    {
        ALL_ENTITIES            ("entities"),
        ENTITIES_BY_TYPE        ("entities"),
        ENTITIES_BY_CHUNK       ("entities"),
        ALL_TILE_ENTITIES       ("TileEntities"),
        TILE_ENTITIES_BY_TYPE   ("TileEntities"),
        TILE_ENTITIES_BY_CHUNK  ("TileEntities");

        private final String typeNamePlural;

        private EntityListType(String typeNamePlural)
        {
            this.typeNamePlural = typeNamePlural;
        }

        public String getTypeNamePlural()
        {
            return this.typeNamePlural;
        }
    }
}
