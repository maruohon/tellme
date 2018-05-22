package fi.dy.masa.tellme.event.datalogging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
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
    protected boolean enabled;

    public LoggerWrapperBase(DataType type)
    {
        this.type = type;
        this.enabled = true;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isOutputTypeEnabled(OutputType type)
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

    public void setOutputTypeEnabled(OutputType type, boolean enable)
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

    @Nullable
    public File dumpData(DataDump.Format format, int dimension)
    {
        if (this.loggedData.size() > 0)
        {
            DataDump dump = this.loggedData.get(0).createDataDump(format);

            for (DataEntryBase<?> entry : this.loggedData)
            {
                entry.addDataToDump(dump);
            }

            return DataDump.dumpDataToFile("logged_data_" + this.type.getArgName() + "_dim_" + dimension, dump.getLines());
        }

        return null;
    }

    public void onChunkEvent(Chunk chunk)
    {
    }

    public void onEntityEvent(Entity entity)
    {
    }

    public enum OutputType
    {
        PRINT,
        LOG
    }
}
