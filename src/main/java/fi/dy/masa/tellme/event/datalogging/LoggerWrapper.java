package fi.dy.masa.tellme.event.datalogging;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryBase;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryChunkEventBase;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryChunkEventLoad;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryChunkEventUnload;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryEntityEvent;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class LoggerWrapper extends LoggerWrapperBase
{
    public LoggerWrapper(DataType type)
    {
        super(type);
    }

    @Override
    public void onChunkEvent(DataType type, Chunk chunk)
    {
        this.handleData(type, this.getChunkDataEntry(type, chunk));
    }

    @Override
    public void onEntityEvent(DataType type, Entity entity)
    {
        this.handleData(type, this.getEntityDataEntry(type, entity));
    }

    private void handleData(DataType type, DataEntryBase<?> data)
    {
        if (data != null)
        {
            if (this.enablePrint)
            {
                TellMe.logger.info(data.getPrintLine());
            }

            if (this.enableLog)
            {
                this.loggedData.add(data);
            }
        }
    }

    @Nullable
    private DataEntryChunkEventBase getChunkDataEntry(DataType type, Chunk chunk)
    {
        switch (type)
        {
            case CHUNK_LOAD:        return new DataEntryChunkEventLoad(chunk);
            case CHUNK_UNLOAD:      return new DataEntryChunkEventUnload(chunk);
            default:                return null;
        }
    }

    @Nullable
    private DataEntryEntityEvent getEntityDataEntry(DataType type, Entity entity)
    {
        switch (type)
        {
            case ENTITY_JOIN_WORLD: return new DataEntryEntityEvent(type, entity);
            default:                return null;
        }
    }
}
