package fi.dy.masa.tellme.event.datalogging;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryBase;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryChunkEvent;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryEntityEvent;

public class DataLogger
{
    private static final DataLogger INSTANCE = new DataLogger();
    private final EnumMap<DataType, Boolean> enabledTypesLog = new EnumMap<DataType, Boolean>(DataType.class);
    private final EnumMap<DataType, Boolean> enabledTypesPrint = new EnumMap<DataType, Boolean>(DataType.class);

    private final EnumMap<DataType, List<DataEntryBase<?>>> loggedData = new EnumMap<DataType, List<DataEntryBase<?>>>(DataType.class);

    private DataLogger()
    {
        for (DataType type : DataType.values())
        {
            this.loggedData.put(type, new ArrayList<>());
        }
    }

    public static DataLogger instance()
    {
        return INSTANCE;
    }

    public void setLoggingEnabled(DataType type, boolean enabled)
    {
        if (enabled)
        {
            this.enabledTypesLog.put(type, Boolean.TRUE);
        }
        else
        {
            this.enabledTypesLog.remove(type);
        }
    }

    public void setPrintingEnabled(DataType type, boolean enabled)
    {
        if (enabled)
        {
            this.enabledTypesPrint.put(type, Boolean.TRUE);
        }
        else
        {
            this.enabledTypesPrint.remove(type);
        }
    }

    public void clearData(DataType type)
    {
        this.loggedData.get(type).clear();
    }

    public void dumpData(DataType type, DataDump.Format format)
    {
        List<DataEntryBase<?>> data = this.loggedData.get(type);

        if (this.enabledTypesLog.get(type) != null && data.size() > 0)
        {
            DataDump dump = data.get(0).createDataDump(format);

            for (DataEntryBase<?> entry : data)
            {
                entry.addDataToDump(dump);
            }

            DataDump.dumpDataToFile("logged_data_" + type, dump.getLines()); // FIXME file name
        }
    }

    public void onChunkEvent(DataType type, Chunk chunk)
    {
        DataEntryChunkEvent data = this.getChunkDataEntry(type, chunk);
        this.handleData(type, data);
    }

    public void onEntityEvent(DataType type, Entity entity)
    {
        DataEntryEntityEvent data = this.getEntityDataEntry(type, entity);
        this.handleData(type, data);
    }

    private void handleData(DataType type, DataEntryBase<?> data)
    {
        if (data != null)
        {
            if (this.enabledTypesPrint.get(type) != null)
            {
                TellMe.logger.info(data.getPrintLine());
            }

            if (this.enabledTypesLog.get(type) != null)
            {
                this.loggedData.get(type).add(data);
            }
        }
    }

    @Nullable
    private DataEntryChunkEvent getChunkDataEntry(DataType type, Chunk chunk)
    {
        switch (type)
        {
            case CHUNK_LOAD:        return new DataEntryChunkEvent(type, chunk);
            case CHUNK_UNLOAD:      return new DataEntryChunkEvent(type, chunk);
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

    public enum DataType
    {
        CHUNK_LOAD              ("chunk-load",          "Chunk Load"),
        CHUNK_UNLOAD            ("chunk-unload",        "Chunk Unload"),
        ENTITY_JOIN_WORLD       ("entity-join-world",   "Entity Join World");

        private final String argName;
        private final String outputName;

        private DataType(String argName, String outputName)
        {
            this.argName = argName;
            this.outputName = outputName;
        }

        public String getOutputName()
        {
            return this.outputName;
        }

        @Nullable
        public static DataType fromArgument(String arg)
        {
            for (DataType type : DataType.values())
            {
                if (type.argName.equals(arg))
                {
                    return type;
                }
            }

            return null;
        }
    }
}
