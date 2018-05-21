package fi.dy.masa.tellme.event.datalogging;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.event.datalogging.DataEntry.DataEntryBase;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class LoggerWrapperBase
{
    protected final DataType type;
    protected final List<DataEntryBase<?>> loggedData = new ArrayList<>();
    protected boolean enablePrint;
    protected boolean enableLog;

    public LoggerWrapperBase(DataType type)
    {
        this.type = type;
    }

    public boolean isEnabled(OutputType type)
    {
        switch (type)
        {
            case PRINT:
                return this.enablePrint;
            case LOG:
                return this.enableLog;
            default:
                return false;
        }
    }

    public void setEnabled(OutputType type, boolean enable)
    {
        switch (type)
        {
            case PRINT:
                this.enablePrint = enable;
                break;
            case LOG:
                this.enableLog = enable;
                break;
        }
    }

    public void clearData()
    {
        this.loggedData.clear();
    }

    public void dumpData(DataDump.Format format)
    {
        if (this.loggedData.size() > 0)
        {
            DataDump dump = this.loggedData.get(0).createDataDump(format);

            for (DataEntryBase<?> entry : this.loggedData)
            {
                entry.addDataToDump(dump);
            }

            DataDump.dumpDataToFile("logged_data_" + this.type.getArgName(), dump.getLines());
        }
    }

    public void onChunkEvent(DataType type, Chunk chunk)
    {
    }

    public void onEntityEvent(DataType type, Entity entity)
    {
    }

    public enum OutputType
    {
        PRINT,
        LOG
    }
}
