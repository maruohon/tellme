package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.WorldUtils;

public class EntityCountDump extends DataDump
{
    protected int emptyChunks;
    protected int unloadedChunks;
    private ChunkProcessor counter;
    private String strFooter;

    private EntityCountDump(int columns)
    {
        super(columns);

        this.setSort(false);
        this.setRepeatTitleAtBottom(false);
    }

    private static EntityCountDump initDump(EntityListType type)
    {
        EntityCountDump entityCountDump = null;

        if (type == EntityListType.ENTITIES_BY_TYPE)
        {
            entityCountDump = new EntityCountDump(2);
            entityCountDump.counter = new EntitiesPerTypeCounter();
            entityCountDump.addHeader("Loaded entities by entity type");
            entityCountDump.addTitle("Entity type", "Count");
            entityCountDump.strFooter = "with no entities.";
        }
        else if (type == EntityListType.ENTITIES_BY_CHUNK)
        {
            entityCountDump = new EntityCountDump(2);
            entityCountDump.counter = new EntitiesPerChunkCounter();
            entityCountDump.addHeader("Loaded entities by chunk");
            entityCountDump.addTitle("Chunk", "Count");
            entityCountDump.strFooter = "with no entities.";
        }
        else if (type == EntityListType.TILEENTITIES_BY_TYPE)
        {
            entityCountDump = new EntityCountDump(3);
            entityCountDump.counter = new TileEntitiesPerTypeCounter();
            entityCountDump.addHeader("Loaded TileEntities by type");
            entityCountDump.addTitle("TileEntity type", "Count", "Is ticking?");
            entityCountDump.strFooter = "with no TileEntities.";
        }
        else if (type == EntityListType.TILEENTITIES_BY_CHUNK)
        {
            entityCountDump = new EntityCountDump(3);
            entityCountDump.counter = new TileEntitiesPerChunkCounter();
            entityCountDump.addHeader("Loaded TileEntities by chunk");
            entityCountDump.addTitle("Chunk", "Total Count", "Ticking");
            entityCountDump.strFooter = "with no TileEntities.";
        }

        return entityCountDump;
    }

    public static List<String> getFormattedEntityCountDumpAll(World world, EntityListType type)
    {
        EntityCountDump entityCountDump = initDump(type);

        entityCountDump.processAllLoadedChunks(world, entityCountDump.counter);
        entityCountDump.setUseColumnSeparator(true);

        entityCountDump.addHeader(String.format("World '%s' (dim: %d)", world.provider.getDimensionType().getName(), world.provider.getDimension()));
        entityCountDump.addHeader(String.format("Loaded chunks: %d", WorldUtils.getLoadedChunkCount(world)));

        if (entityCountDump.emptyChunks != 0)
        {
            entityCountDump.addFooter(String.format("There were %d loaded chunks", entityCountDump.emptyChunks));
            entityCountDump.addFooter(entityCountDump.strFooter);
        }

        return entityCountDump.getLines();
    }

    public static List<String> getFormattedEntityCountDumpArea(World world, EntityListType type, ChunkPos pos1In, ChunkPos pos2In)
    {
        EntityCountDump entityCountDump = initDump(type);
        ChunkPos pos1 = new ChunkPos(Math.min(pos1In.chunkXPos, pos2In.chunkXPos), Math.min(pos1In.chunkZPos, pos2In.chunkZPos));
        ChunkPos pos2 = new ChunkPos(Math.max(pos1In.chunkXPos, pos2In.chunkXPos), Math.max(pos1In.chunkZPos, pos2In.chunkZPos));

        entityCountDump.processChunksInArea(world, entityCountDump.counter, pos1, pos2);
        entityCountDump.setUseColumnSeparator(true);

        entityCountDump.addHeader(String.format("World '%s' (dim: %d)", world.provider.getDimensionType().getName(), world.provider.getDimension()));

        if (pos1.equals(pos2))
        {
            entityCountDump.addHeader(String.format("Chunk: [%d, %d]", pos1.chunkXPos, pos1.chunkZPos));
        }
        else
        {
            entityCountDump.addHeader(String.format("Chunks: [%d, %d] to [%d, %d]", pos1.chunkXPos, pos1.chunkZPos, pos2.chunkXPos, pos2.chunkZPos));
        }

        if (entityCountDump.emptyChunks != 0)
        {
            entityCountDump.addFooter(String.format("There were %d chunks in the selected area", entityCountDump.emptyChunks));
            entityCountDump.addFooter(entityCountDump.strFooter);
        }

        if (entityCountDump.unloadedChunks != 0)
        {
            entityCountDump.addFooter(String.format("There were %d unloaded chunks in the selected area.", entityCountDump.unloadedChunks));
        }

        return entityCountDump.getLines();
    }

    private void processAllLoadedChunks(World world, ChunkProcessor chunkProcessor)
    {
        IChunkProvider provider = world.getChunkProvider();

        if (provider instanceof ChunkProviderServer)
        {
            Collection<Chunk> loadedChunks = ((ChunkProviderServer) provider).getLoadedChunks();

            for (Chunk chunk : loadedChunks)
            {
                chunkProcessor.processChunk(chunk);
            }
        }

        chunkProcessor.getData(this);
        this.emptyChunks = chunkProcessor.emptyChunks;
    }

    private void processChunksInArea(World world, ChunkProcessor chunkProcessor, ChunkPos pos1, ChunkPos pos2)
    {
        IChunkProvider provider = world.getChunkProvider();

        for (int chunkZ = pos1.chunkZPos; chunkZ <= pos2.chunkZPos; chunkZ++)
        {
            for (int chunkX = pos1.chunkXPos; chunkX <= pos2.chunkXPos; chunkX++)
            {
                Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);

                if (chunk != null)
                {
                    chunkProcessor.processChunk(chunk);
                }
                else
                {
                    this.unloadedChunks++;
                }
            }
        }

        chunkProcessor.getData(this);
        this.emptyChunks = chunkProcessor.emptyChunks;
    }

    public static abstract class ChunkProcessor
    {
        protected int emptyChunks;

        public abstract void processChunk(Chunk chunk);

        public abstract void getData(EntityCountDump dump);
    }

    public static class EntitiesPerTypeCounter extends ChunkProcessor
    {
        private Map<Class <? extends Entity>, Integer> perTypeCount = new HashMap<Class <? extends Entity>, Integer>();
        private int totalCount;

        @Override
        public void processChunk(Chunk chunk)
        {
            ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
            int total = 0;

            for (int i = 0; i < entityLists.length; i++)
            {
                Iterator<Entity> iter = entityLists[i].iterator();
                total += entityLists[i].size();

                while (iter.hasNext())
                {
                    Entity entity = iter.next();
                    Integer countInt = this.perTypeCount.get(entity.getClass());
                    int count = countInt != null ? countInt + 1 : 1;
                    this.perTypeCount.put(entity.getClass(), count);
                }
            }

            if (total == 0)
            {
                this.emptyChunks++;
            }

            this.totalCount += total;
        }

        @Override
        public void getData(EntityCountDump dump)
        {
            List<EntitiesPerTypeHolder> counts = new ArrayList<EntitiesPerTypeHolder>();

            for (Class <? extends Entity> clazz : this.perTypeCount.keySet())
            {
                counts.add(new EntitiesPerTypeHolder(clazz, this.perTypeCount.get(clazz)));
            }

            Collections.sort(counts);

            for (EntitiesPerTypeHolder holder : counts)
            {
                dump.addData(EntityInfo.getEntityNameFromClass(holder.clazz), String.valueOf(holder.count));
            }

            dump.addFooter(String.format("In total there were %d loaded entities.", this.totalCount));
        }
    }

    public static class EntitiesPerChunkCounter extends ChunkProcessor
    {
        private Map<ChunkPos, Integer> perChunkCount = new HashMap<ChunkPos, Integer>();
        private int totalCount;

        @Override
        public void processChunk(Chunk chunk)
        {
            ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
            int total = 0;

            for (int i = 0; i < entityLists.length; i++)
            {
                total += entityLists[i].size();
            }

            if (total == 0)
            {
                this.emptyChunks++;
            }
            else
            {
                this.perChunkCount.put(chunk.getPos(), total);
                this.totalCount += total;
            }
        }

        @Override
        public void getData(EntityCountDump dump)
        {
            List<CountsPerChunkHolder> counts = new ArrayList<CountsPerChunkHolder>();

            for (ChunkPos pos : this.perChunkCount.keySet())
            {
                counts.add(new CountsPerChunkHolder(pos, this.perChunkCount.get(pos)));
            }

            Collections.sort(counts);

            for (CountsPerChunkHolder holder : counts)
            {
                dump.addData(String.format("[%5d, %5d]", holder.pos.chunkXPos, holder.pos.chunkZPos), String.valueOf(holder.count));
            }

            dump.addFooter(String.format("In total there were %d loaded entities.", this.totalCount));
        }
    }

    public static class TileEntitiesPerTypeCounter extends ChunkProcessor
    {
        private Map<Class <? extends TileEntity>, Integer> perTypeCount = new HashMap<Class <? extends TileEntity>, Integer>();
        private int totalCount;

        @Override
        public void processChunk(Chunk chunk)
        {
            Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
            int total = map.size();

            for (TileEntity te : map.values())
            {
                Integer countInt = this.perTypeCount.get(te.getClass());
                int count = countInt != null ? countInt + 1 : 1;
                this.perTypeCount.put(te.getClass(), count);
            }

            if (total == 0)
            {
                this.emptyChunks++;
            }

            this.totalCount += total;
        }

        @Override
        public void getData(EntityCountDump dump)
        {
            List<TileEntitiesPerTypeHolder> counts = new ArrayList<TileEntitiesPerTypeHolder>();

            for (Class <? extends TileEntity> clazz : this.perTypeCount.keySet())
            {
                counts.add(new TileEntitiesPerTypeHolder(clazz, this.perTypeCount.get(clazz)));
            }

            Collections.sort(counts);

            for (TileEntitiesPerTypeHolder holder : counts)
            {
                String ticking = ITickable.class.isAssignableFrom(holder.clazz) ? "true" : "false";
                dump.addData(holder.clazz.getName(), String.valueOf(holder.count), ticking);
            }

            dump.addFooter(String.format("In total there were %d loaded TEs.", this.totalCount));
        }
    }

    public static class TileEntitiesPerChunkCounter extends ChunkProcessor
    {
        private Map<ChunkPos, Integer> perChunkTotalCount = new HashMap<ChunkPos, Integer>();
        private Map<ChunkPos, Integer> perChunkTickingCount = new HashMap<ChunkPos, Integer>();
        private int totalCount;
        private int tickingCount;

        @Override
        public void processChunk(Chunk chunk)
        {
            Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
            ChunkPos pos = chunk.getPos();
            int count = chunk.getTileEntityMap().size();

            if (count == 0)
            {
                this.emptyChunks++;
            }
            else
            {
                int tickingCount = 0;

                for (TileEntity te : map.values())
                {
                    if (te instanceof ITickable)
                    {
                        tickingCount++;
                    }
                }

                this.perChunkTotalCount.put(pos, count);
                this.perChunkTickingCount.put(pos, tickingCount);
                this.totalCount += count;
                this.tickingCount += tickingCount;
            }
        }

        @Override
        public void getData(EntityCountDump dump)
        {
            List<TileEntityCountsPerChunkHolder> counts = new ArrayList<TileEntityCountsPerChunkHolder>();

            for (ChunkPos pos : this.perChunkTotalCount.keySet())
            {
                counts.add(new TileEntityCountsPerChunkHolder(pos, this.perChunkTotalCount.get(pos), this.perChunkTickingCount.get(pos)));
            }

            Collections.sort(counts);

            for (TileEntityCountsPerChunkHolder holder : counts)
            {
                dump.addData(String.format("[%5d, %5d]", holder.pos.chunkXPos, holder.pos.chunkZPos),
                        String.valueOf(holder.count), String.valueOf(holder.tickingCount));
            }

            dump.addFooter(String.format("In total there were %d loaded", this.totalCount));
            dump.addFooter(String.format("TileEntities, of which %d are ticking.", this.tickingCount));
        }
    }

    public static class EntitiesPerTypeHolder implements Comparable<EntitiesPerTypeHolder>
    {
        public final Class <? extends Entity> clazz;
        public final int count;

        public EntitiesPerTypeHolder(Class <? extends Entity> clazz, int count)
        {
            this.clazz = clazz;
            this.count = count;
        }

        @Override
        public int compareTo(EntitiesPerTypeHolder other)
        {
            if (this.count == other.count)
            {
                String nameThis = EntityInfo.getEntityNameFromClass(this.clazz);
                String nameOther = EntityInfo.getEntityNameFromClass(other.clazz);

                if (nameThis == null)
                {
                    nameThis = this.clazz.getSimpleName();
                }

                if (nameOther == null)
                {
                    nameOther = other.clazz.getSimpleName();
                }

                return nameThis.compareTo(nameOther);
            }

            return this.count > other.count ? -1 : 1;
        }
    }

    public static class CountsPerChunkHolder implements Comparable<CountsPerChunkHolder>
    {
        public final ChunkPos pos;
        public final int count;

        public CountsPerChunkHolder(ChunkPos pos, int count)
        {
            this.pos = pos;
            this.count = count;
        }

        @Override
        public int compareTo(CountsPerChunkHolder other)
        {
            if (this.count == other.count)
            {
                return 0;
            }

            return this.count > other.count ? -1 : 1;
        }
    }

    public static class TileEntityCountsPerChunkHolder extends CountsPerChunkHolder
    {
        public final int tickingCount;

        public TileEntityCountsPerChunkHolder(ChunkPos pos, int totalCount, int tickingCount)
        {
            super(pos, totalCount);
            this.tickingCount = tickingCount;
        }
    }

    public static class TileEntitiesPerTypeHolder implements Comparable<TileEntitiesPerTypeHolder>
    {
        public final Class <? extends TileEntity> clazz;
        public final int count;

        public TileEntitiesPerTypeHolder(Class <? extends TileEntity> clazz, int count)
        {
            this.clazz = clazz;
            this.count = count;
        }

        @Override
        public int compareTo(TileEntitiesPerTypeHolder other)
        {
            if (this.count == other.count)
            {
                String nameThis = this.clazz.getName();
                String nameOther = other.clazz.getName();
                return nameThis.compareTo(nameOther);
            }

            return this.count > other.count ? -1 : 1;
        }
    }

    public enum EntityListType
    {
        ENTITIES_BY_TYPE,
        ENTITIES_BY_CHUNK,
        TILEENTITIES_BY_TYPE,
        TILEENTITIES_BY_CHUNK;
    }
}
