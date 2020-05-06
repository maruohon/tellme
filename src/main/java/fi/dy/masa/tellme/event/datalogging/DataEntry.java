package fi.dy.masa.tellme.event.datalogging;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class DataEntry
{
    public static abstract class DataEntryBase<P>
    {
        private final String dimension;
        private final long worldTick;
        private final long systemTime;
        private final P position;

        private DataEntryBase(World world, P position)
        {
            this.dimension = world.getDimension().getType().getRegistryName().toString();
            this.worldTick = world.getGameTime();
            this.systemTime = System.currentTimeMillis();
            this.position = position;
        }

        public String getDimension()
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

        protected abstract String getDisplayName();

        public abstract String getPrintLine();

        public abstract DataDump createDataDump(DataDump.Format format);

        public abstract void addDataToDump(DataDump dump);
    }

    public static abstract class DataEntryChunkEventBase extends DataEntryBase<ChunkPos>
    {
        public DataEntryChunkEventBase(IChunk chunk)
        {
            super(chunk.getWorldForge().getWorld(), chunk.getPos());
        }

        @Override
        public String getPrintLine()
        {
            ChunkPos pos = this.getPosition();

            return String.format("%13s ; DIM: %5d, Tick: %9d, Time: %11d, ChunkPos: [%5d, %5d]",
                    this.getDisplayName(), this.getDimension(), this.getWorldTick(), this.getSystemTime(), pos.x, pos.z);
        }

        @Override
        public DataDump createDataDump(DataDump.Format format)
        {
            DataDump dump = new DataDump(3, format);
            dump.addTitle("World tick", "System time", "Chunk");

            dump.setColumnProperties(0, DataDump.Alignment.RIGHT, true); // tick
            dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true); // time

            return dump;
        }

        @Override
        public void addDataToDump(DataDump dump)
        {
            ChunkPos pos = this.getPosition();

            dump.addData(
                    String.valueOf(this.getWorldTick()),
                    String.valueOf(this.getSystemTime()),
                    String.format("[%5d, %5d]", pos.x, pos.z));
        }
    }

    public static class DataEntryChunkEventLoad extends DataEntryChunkEventBase
    {
        public DataEntryChunkEventLoad(IChunk chunk)
        {
            super(chunk);
        }

        @Override
        protected String getDisplayName()
        {
            return "Chunk Load";
        }
    }

    public static class DataEntryChunkEventUnload extends DataEntryChunkEventBase
    {
        public DataEntryChunkEventUnload(IChunk chunk)
        {
            super(chunk);
        }

        @Override
        protected String getDisplayName()
        {
            return "Chunk Unload";
        }
    }

    public static class DataEntryEntityEvent extends DataEntryBase<Vec3d>
    {
        private final String entityName;

        public DataEntryEntityEvent(Entity entity)
        {
            super(entity.getEntityWorld(), entity.getPositionVector());

            this.entityName = entity.getName().getString();
        }

        @Override
        protected String getDisplayName()
        {
            return "Entity Join World";
        }

        @Override
        public String getPrintLine()
        {
            Vec3d pos = this.getPosition();

            return String.format("%s ; DIM: %3d, Tick: %d, Time: %d, Pos: [%.2f, %.2f, %.2f]",
                    this.getDisplayName(), this.getDimension(), this.getWorldTick(), this.getSystemTime(), pos.x, pos.y, pos.z);
        }

        @Override
        public DataDump createDataDump(DataDump.Format format)
        {
            DataDump dump = new DataDump(4, format);
            dump.addTitle("World tick", "System time", "Position", "Name");

            dump.setColumnProperties(0, DataDump.Alignment.RIGHT, true); // tick
            dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true); // time

            return dump;
        }

        @Override
        public void addDataToDump(DataDump dump)
        {
            Vec3d pos = this.getPosition();

            dump.addData(
                    String.valueOf(this.getWorldTick()),
                    String.valueOf(this.getSystemTime()),
                    String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z),
                    this.entityName);
        }
    }
}
