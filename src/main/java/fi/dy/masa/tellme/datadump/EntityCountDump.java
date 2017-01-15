package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import fi.dy.masa.tellme.util.WorldUtils;

public class EntityCountDump extends DataDump
{
    protected int emptyChunks;

    private EntityCountDump(int columns)
    {
        super(columns);

        this.setSort(false);
        this.setRepeatTitleAtBottom(false);
    }

    public static List<String> getFormattedEntityCountDump(World world, EntityListType type)
    {
        EntityCountDump entityCountDump = null;
        String strFooter = "";
        ChunkProcessor counter = null;

        if (type == EntityListType.ENTITIES_BY_TYPE)
        {
            entityCountDump = new EntityCountDump(2);
            counter = new EntitiesPerTypeCounter();
            entityCountDump.addHeader("Loaded entities by entity type");
            entityCountDump.addTitle("Entity type", "Count");
            strFooter = "with no entities";
        }
        else if (type == EntityListType.ENTITIES_BY_CHUNK)
        {
            entityCountDump = new EntityCountDump(2);
            counter = new EntitiesPerChunkCounter();
            entityCountDump.addHeader("Loaded entities by chunk");
            entityCountDump.addTitle("Chunk", "Count");
            strFooter = "with no entities";
        }
        else if (type == EntityListType.TILEENTITIES_BY_TYPE)
        {
            entityCountDump = new EntityCountDump(3);
            counter = new TileEntitiesPerTypeCounter();
            entityCountDump.addHeader("Loaded TileEntities by type");
            entityCountDump.addTitle("TileEntity type", "Count", "Is ticking?");
            strFooter = "with no TileEntities";
        }
        else if (type == EntityListType.TILEENTITIES_BY_CHUNK)
        {
            entityCountDump = new EntityCountDump(3);
            counter = new TileEntitiesPerChunkCounter();
            entityCountDump.addHeader("Loaded TileEntities by chunk");
            entityCountDump.addTitle("Chunk", "Total Count", "Ticking");
            strFooter = "with no TileEntities";
        }

        entityCountDump.processLoadedChunks(world, counter);
        entityCountDump.setUseColumnSeparator(true);

        entityCountDump.addHeader(String.format("World '%s' (dim: %d)", world.provider.getDimensionType().getName(), world.provider.getDimension()));
        entityCountDump.addHeader(String.format("Loaded chunks: %d", WorldUtils.getLoadedChunkCount(world)));

        if (entityCountDump.emptyChunks != 0)
        {
            entityCountDump.addFooter(String.format("There were also %d loaded chunks", entityCountDump.emptyChunks));
            entityCountDump.addFooter(strFooter);
        }

        return entityCountDump.getLines();
    }

    protected void processLoadedChunks(World world, ChunkProcessor chunkProcessor)
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

    public static abstract class ChunkProcessor
    {
        protected int emptyChunks;

        public abstract void processChunk(Chunk chunk);

        public abstract void getData(EntityCountDump dump);
    }

    public static class EntitiesPerTypeCounter extends ChunkProcessor
    {
        private Map<Class <? extends Entity>, Integer> perTypeCount = new HashMap<Class <? extends Entity>, Integer>();

        @Override
        public void processChunk(Chunk chunk)
        {
            ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
            int total = 0;

            for (int i = 0; i < entityLists.length; i++)
            {
                Iterator<Entity> iter = entityLists[i].iterator();

                while (iter.hasNext())
                {
                    Entity entity = iter.next();
                    Integer countInt = this.perTypeCount.get(entity.getClass());
                    int count = countInt != null ? countInt + 1 : 1;
                    this.perTypeCount.put(entity.getClass(), count);
                    total++;
                }
            }

            if (total == 0)
            {
                this.emptyChunks++;
            }
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
                String name = EntityList.getEntityStringFromClass(holder.clazz);

                if (name == null)
                {
                    name = holder.clazz.getSimpleName();
                }

                dump.addData(name, String.valueOf(holder.count));
            }
        }
    }

    public static class EntitiesPerChunkCounter extends ChunkProcessor
    {
        private Map<ChunkPos, Integer> perChunkCount = new HashMap<ChunkPos, Integer>();

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
                this.perChunkCount.put(chunk.getChunkCoordIntPair(), total);
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
        }
    }

    public static class TileEntitiesPerTypeCounter extends ChunkProcessor
    {
        private Map<Class <? extends TileEntity>, Integer> perTypeCount = new HashMap<Class <? extends TileEntity>, Integer>();

        @Override
        public void processChunk(Chunk chunk)
        {
            Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
            int total = 0;

            for (TileEntity te : map.values())
            {
                Integer countInt = this.perTypeCount.get(te.getClass());
                int count = countInt != null ? countInt + 1 : 1;
                this.perTypeCount.put(te.getClass(), count);
                total++;
            }

            if (total == 0)
            {
                this.emptyChunks++;
            }
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
        }
    }

    public static class TileEntitiesPerChunkCounter extends ChunkProcessor
    {
        private Map<ChunkPos, Integer> perChunkTotalCount = new HashMap<ChunkPos, Integer>();
        private Map<ChunkPos, Integer> perChunkTickingCount = new HashMap<ChunkPos, Integer>();

        @Override
        public void processChunk(Chunk chunk)
        {
            Map<BlockPos, TileEntity> map = chunk.getTileEntityMap();
            ChunkPos pos = chunk.getChunkCoordIntPair();
            int totalCount = chunk.getTileEntityMap().size();

            if (totalCount == 0)
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

                this.perChunkTotalCount.put(pos, totalCount);
                this.perChunkTickingCount.put(pos, tickingCount);
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
                String nameThis = EntityList.getEntityStringFromClass(this.clazz);
                String nameOther = EntityList.getEntityStringFromClass(other.clazz);

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
