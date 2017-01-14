package fi.dy.masa.tellme.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;

public class EntityInfo
{
    private static List<String> getBasicEntityInfo(Entity target)
    {
        List<String> lines = new ArrayList<String>();

        lines.add("Entity: " + target.getClass().getSimpleName() + " (entityId: " + target.getEntityId() + ")");

        return lines;
    }

    private static List<String> getFullEntityInfo(Entity target)
    {
        List<String> lines = getBasicEntityInfo(target);
        NBTTagCompound nbt = new NBTTagCompound();

        target.writeToNBT(nbt);
        NBTFormatter.getPrettyFormattedNBT(lines, nbt);

        return lines;
    }

    public static void printBasicEntityInfoToChat(EntityPlayer player, Entity target)
    {
        for (String line : getBasicEntityInfo(target))
        {
            player.sendMessage(new TextComponentString(line));
        }
    }

    public static void printFullEntityInfoToConsole(EntityPlayer player, Entity target)
    {
        List<String> lines = getFullEntityInfo(target);

        for (String line : lines)
        {
            TellMe.logger.info(line);
        }
    }

    public static void printEntityInfo(EntityPlayer player, Entity target, boolean dumpToFile)
    {
        EntityInfo.printBasicEntityInfoToChat(player, target);

        if (dumpToFile)
        {
            EntityInfo.dumpFullEntityInfoToFile(player, target);
        }
        else
        {
            EntityInfo.printFullEntityInfoToConsole(player, target);
        }
    }

    public static void dumpFullEntityInfoToFile(EntityPlayer player, Entity target)
    {
        File f = DataDump.dumpDataToFile("entity_data", getFullEntityInfo(target));
        player.sendMessage(new TextComponentString("Output written to file " + f.getName()));
    }

    public static List<String> getEntityCounts(World world, EntityListType type)
    {
        List<String> lines = new ArrayList<String>();

        if (type == EntityListType.BY_ENTITY_TYPE)
        {
            return getEntityCountPerEntityType(world);
        }
        else if (type == EntityListType.BY_CHUNK)
        {
            return getEntityCountPerChunk(world);
        }

        return lines;
    }

    private static List<String> getEntityCountPerEntityType(World world)
    {
        List<String> lines = new ArrayList<String>();
        Map<Class <? extends Entity>, Integer> perTypeCount = new HashMap<Class <? extends Entity>, Integer>();
        List<Entity> entities = world.getEntities(Entity.class, EntitySelectors.IS_ALIVE);

        for (Entity entity : entities)
        {
            Integer countInt = perTypeCount.get(entity.getClass());
            int count = countInt != null ? countInt + 1 : 1;
            perTypeCount.put(entity.getClass(), count);
        }

        lines.add("---------------------------------------------------------------");
        lines.add("  Loaded entities by entity type");
        lines.add("---------------------------------------------------------------");
        lines.add(String.format("World '%s' (dim: %d):", world.provider.getDimensionType().name(), world.provider.getDimension()));
        List<EntitiesPerType> counts = new ArrayList<EntitiesPerType>();

        for (Class <? extends Entity> clazz : perTypeCount.keySet())
        {
            counts.add(new EntitiesPerType(clazz, perTypeCount.get(clazz)));
        }

        Collections.sort(counts);

        for (EntitiesPerType holder : counts)
        {
            EntityEntry entry = EntityRegistry.getEntry(holder.clazz);
            String name;

            if (entry == null || entry.getName() == null)
            {
                name = holder.clazz.getSimpleName();
            }
            else
            {
                name = entry.getName();
            }

            lines.add(String.format("%18s: %5d entities", name, holder.count));
        }

        lines.add("---------------------------------------------------------------");

        return lines;
    }

    private static List<String> getEntityCountPerChunk(World world)
    {
        List<String> lines = new ArrayList<String>();
        IChunkProvider provider = world.getChunkProvider();

        if (provider instanceof ChunkProviderServer)
        {
            Collection<Chunk> loadedChunks = ((ChunkProviderServer) provider).getLoadedChunks();
            Map<ChunkPos, Integer> perChunkCount = new HashMap<ChunkPos, Integer>();

            for (Chunk chunk : loadedChunks)
            {
                int count = 0;

                for (int i = 0; i < chunk.getEntityLists().length; i++)
                {
                    Iterator<Entity> iter = chunk.getEntityLists()[i].iterator();

                    while (iter.hasNext())
                    {
                        iter.next();
                        count++;
                    }
                }

                perChunkCount.put(chunk.getPos(), count);
            }

            lines.add("---------------------------------------------------------------");
            lines.add("  Loaded entities by chunk");
            lines.add("---------------------------------------------------------------");
            lines.add(String.format("World '%s' (dim: %d):", world.provider.getDimensionType().name(), world.provider.getDimension()));
            List<EntitiesPerChunk> counts = new ArrayList<EntitiesPerChunk>();
            int countNoEntities = 0;

            for (ChunkPos pos : perChunkCount.keySet())
            {
                int count = perChunkCount.get(pos);

                if (count > 0)
                {
                    counts.add(new EntitiesPerChunk(pos, count));
                }
                else
                {
                    countNoEntities++;
                }
            }

            Collections.sort(counts);

            for (EntitiesPerChunk holder : counts)
            {
                lines.add(String.format("Chunk [%5d, %5d] has %4d entities", holder.pos.chunkXPos, holder.pos.chunkZPos, holder.count));
            }

            lines.add(String.format("There were also %d loaded chunks with no entities", countNoEntities));
            lines.add("---------------------------------------------------------------");
        }

        return lines;
    }

    public static class EntitiesPerType implements Comparable<EntitiesPerType>
    {
        public final Class <? extends Entity> clazz;
        public final int count;

        public EntitiesPerType(Class <? extends Entity> clazz, int count)
        {
            this.clazz = clazz;
            this.count = count;
        }

        @Override
        public int compareTo(EntitiesPerType other)
        {
            if (this.count == other.count)
            {
                EntityEntry entryThis = EntityRegistry.getEntry(this.clazz);
                EntityEntry entryOther = EntityRegistry.getEntry(other.clazz);
                String nameThis;
                String nameOther;

                if (entryThis == null || entryThis.getName() == null)
                {
                    nameThis = this.clazz.getSimpleName();
                }
                else
                {
                    nameThis = entryThis.getName();
                }

                if (entryOther == null || entryOther.getName() == null)
                {
                    nameOther = other.clazz.getSimpleName();
                }
                else
                {
                    nameOther = entryOther.getName();
                }

                return nameThis.compareTo(nameOther);
            }

            return this.count > other.count ? -1 : 1;
        }
    }

    public static class EntitiesPerChunk implements Comparable<EntitiesPerChunk>
    {
        public final ChunkPos pos;
        public final int count;

        public EntitiesPerChunk(ChunkPos pos, int count)
        {
            this.pos = pos;
            this.count = count;
        }

        @Override
        public int compareTo(EntitiesPerChunk other)
        {
            if (this.count == other.count)
            {
                return 0;
            }

            return this.count > other.count ? -1 : 1;
        }
    }

    public enum EntityListType
    {
        BY_ENTITY_TYPE,
        BY_CHUNK;
    }
}
