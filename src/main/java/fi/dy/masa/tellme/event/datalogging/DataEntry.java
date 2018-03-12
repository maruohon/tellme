package fi.dy.masa.tellme.event.datalogging;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.event.datalogging.DataLogger.DataType;

public class DataEntry
{
    public static abstract class DataEntryBase<P>
    {
        private final DataType type;
        private final int dimension;
        private final long worldTick;
        private final long systemTime;
        private final P position;

        private DataEntryBase(DataType type, World world, P position)
        {
            this.type = type;
            this.dimension = world.provider.getDimension();
            this.worldTick = world.getTotalWorldTime();
            this.systemTime = System.currentTimeMillis();
            this.position = position;
        }

        public int getDimension()
        {
            return this.dimension;
        }

        public long getWorldTick()
        {
            return this.worldTick;
        }

        public long getSystemTime()
        {
            return this.systemTime;
        }

        public P getPosition()
        {
            return this.position;
        }

        public DataType getType()
        {
            return this.type;
        }

        public abstract String getPrintLine();

        public abstract DataDump createDataDump(DataDump.Format format);

        public abstract void addDataToDump(DataDump dump);
    }

    public static class DataEntryChunkEvent extends DataEntryBase<ChunkPos>
    {
        public DataEntryChunkEvent(DataType type, Chunk chunk)
        {
            super(type, chunk.getWorld(), chunk.getPos());
        }

        @Override
        public String getPrintLine()
        {
            ChunkPos pos = this.getPosition();

            return String.format("%s ; DIM: %3d, Tick: %d, Time: %d, ChunkPos: [%5d, %5d]",
                    this.getType().getOutputName(), this.getDimension(), this.getWorldTick(), this.getSystemTime(), pos.x, pos.z);
        }

        @Override
        public DataDump createDataDump(DataDump.Format format)
        {
            DataDump dump = new DataDump(4, format);
            dump.addTitle("Dimension", "World tick", "System time", "Chunk");

            dump.setColumnProperties(0, DataDump.Alignment.RIGHT, true); // dim
            dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true); // tick
            dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // time

            dump.setUseColumnSeparator(true);

            return dump;
        }

        @Override
        public void addDataToDump(DataDump dump)
        {
            ChunkPos pos = this.getPosition();

            dump.addData(
                    String.valueOf(this.getDimension()),
                    String.valueOf(this.getWorldTick()),
                    String.valueOf(this.getSystemTime()),
                    String.format("[%5d, %5d]", pos.x, pos.z));
        }
    }

    public static class DataEntryEntityEvent extends DataEntryBase<Vec3d>
    {
        private final String entityName;

        public DataEntryEntityEvent(DataType type, Entity entity)
        {
            super(type, entity.getEntityWorld(), entity.getPositionVector());

            this.entityName = entity.getName();
        }

        @Override
        public String getPrintLine()
        {
            Vec3d pos = this.getPosition();

            return String.format("%s ; DIM: %3d, Tick: %d, Time: %d, Pos: [%.2f, %.2f, %.2f]",
                    this.getType().getOutputName(), this.getDimension(), this.getWorldTick(), this.getSystemTime(), pos.x, pos.y, pos.z);
        }

        @Override
        public DataDump createDataDump(DataDump.Format format)
        {
            DataDump dump = new DataDump(4, format);
            dump.addTitle("Dimension", "World tick", "System time", "Position", "Name");

            dump.setColumnProperties(0, DataDump.Alignment.RIGHT, true); // dim
            dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true); // tick
            dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true); // time

            dump.setUseColumnSeparator(true);

            return dump;
        }

        @Override
        public void addDataToDump(DataDump dump)
        {
            Vec3d pos = this.getPosition();

            dump.addData(
                    String.valueOf(this.getDimension()),
                    String.valueOf(this.getWorldTick()),
                    String.valueOf(this.getSystemTime()),
                    String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z),
                    this.entityName);
        }
    }
}
