package fi.dy.masa.tellme.event.datalogging;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.malilib.util.EntityUtils;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryChunkEventBase;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryChunkEventLoad;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryChunkEventUnload;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryEntityEvent;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class LoggerWrapper extends LoggerBase
{
    private static final Pattern PATTERN_CHUNK_POS = Pattern.compile("(-?[0-9]+),(-?[0-9]+)");

    protected Set<ChunkPos> chunkFilters = new HashSet<>();

    public LoggerWrapper(DataType type)
    {
        super(type);
    }

    @Override
    public void addFilters(String[] filters)
    {
        for (String str : filters)
        {
            Matcher matcher = PATTERN_CHUNK_POS.matcher(str);

            if (matcher.matches())
            {
                try
                {
                    int x = Integer.parseInt(matcher.group(1));
                    int z = Integer.parseInt(matcher.group(2));
                    this.chunkFilters.add(new ChunkPos(x, z));
                }
                catch (Exception e) {}
            }
        }
    }

    @Override
    public void removeFilters(String[] filters)
    {
        for (String str : filters)
        {
            Matcher matcher = PATTERN_CHUNK_POS.matcher(str);

            if (matcher.matches())
            {
                try
                {
                    int x = Integer.parseInt(matcher.group(1));
                    int z = Integer.parseInt(matcher.group(2));
                    this.chunkFilters.remove(new ChunkPos(x, z));
                }
                catch (Exception e) {}
            }
        }
    }

    @Override
    public void onChunkEvent(Chunk chunk)
    {
        if (this.enabled && (this.enablePrint || this.enableLog) &&
            (this.useFilter == false || this.chunkFilters.contains(new ChunkPos(chunk.x, chunk.z))))
        {
            this.handleData(this.createChunkDataEntry(chunk));
        }
    }

    @Override
    public void onEntityEvent(Entity entity)
    {
        if (this.enabled && (this.enablePrint || this.enableLog) &&
            (this.useFilter == false || this.chunkFilters.contains(new ChunkPos(EntityUtils.getChunkX(entity), EntityUtils.getChunkZ(entity)))))
        {
            this.handleData(this.createEntityDataEntry(entity));
        }
    }

    @Nullable
    private DataEntryChunkEventBase createChunkDataEntry(Chunk chunk)
    {
        switch (this.type)
        {
            case CHUNK_LOAD:        return new DataEntryChunkEventLoad(chunk);
            case CHUNK_UNLOAD:      return new DataEntryChunkEventUnload(chunk);
            default:                return null;
        }
    }

    @Nullable
    private DataEntryEntityEvent createEntityDataEntry(Entity entity)
    {
        switch (this.type)
        {
            case ENTITY_JOIN_WORLD: return new DataEntryEntityEvent(entity);
            default:                return null;
        }
    }
}
