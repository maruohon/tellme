package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public abstract class LocateBase extends ChunkProcessorAllChunks
{
    protected static final String FMT_COORD_2 = "%.2f";
    protected static final String FMT_REGION = "r.%d.%d";
    protected static final String FMT_CHUNK = "%d, %d";
    protected static final String FMT_CHUNK_5 = "%5d, %5d";
    protected static final String FMT_COORDS = "x = %.2f, y = %.2f, z = %.2f";
    protected static final String FMT_COORDS_8 = "x = %8.2f, y = %5.2f, z = %8.2f";
    protected static final DynamicCommandExceptionType INVALID_NAME_EXCEPTION = new DynamicCommandExceptionType((v) -> new TextComponent("Invalid name: " + v));

    protected final List<LocationData> data = new ArrayList<>();
    protected final DataDump.Format format;
    protected boolean printDimension;

    protected LocateBase(DataDump.Format format)
    {
        this.format = format;
    }

    public LocateBase setPrintDimension(boolean printDimension)
    {
        this.printDimension = printDimension;
        return this;
    }

    public List<String> getLines()
    {
        int columnCount = this.format == Format.CSV ? 8 : 4;

        if (this.printDimension)
        {
            columnCount += 1;
        }

        DataDump dump = new DataDump(columnCount, this.format);

        for (LocationData entry : this.data)
        {
            this.addLine(dump, entry);
        }

        if (this.format == Format.CSV)
        {
            if (this.printDimension)
            {
                dump.addTitle("ID", "Dim", "RX", "RZ", "CX", "CZ", "x", "y", "z");
            }
            else
            {
                dump.addTitle("ID", "RX", "RZ", "CX", "CZ", "x", "y", "z");
            }
        }
        else
        {
            if (this.printDimension)
            {
                dump.addTitle("ID", "Dim", "Region", "Chunk", "Location");
            }
            else
            {
                dump.addTitle("ID", "Region", "Chunk", "Location");
            }
        }

        return dump.getLines();
    }

    protected void addLine(DataDump dump, LocationData data)
    {
        Vec3 pos = data.pos;
        int rx = ((int) pos.x) >> 9;
        int rz = ((int) pos.z) >> 9;
        int cx = ((int) pos.x) >> 4;
        int cz = ((int) pos.z) >> 4;

        if (this.format == Format.CSV)
        {
            String fmtCoord = FMT_COORD_2;

            if (this.printDimension)
            {
                dump.addData(data.name,
                             data.dimension,
                             String.valueOf(rx), String.valueOf(rz),
                             String.valueOf(cx), String.valueOf(cz),
                             String.format(fmtCoord, pos.x), String.format(fmtCoord, pos.y), String.format(fmtCoord, pos.z));
            }
            else
            {
                dump.addData(data.name,
                             String.valueOf(rx), String.valueOf(rz),
                             String.valueOf(cx), String.valueOf(cz),
                             String.format(fmtCoord, pos.x), String.format(fmtCoord, pos.y), String.format(fmtCoord, pos.z));
            }
        }
        else
        {
            String fmtRegion = FMT_REGION;
            String fmtChunk = this.format == Format.ASCII ? FMT_CHUNK_5 : FMT_CHUNK;
            String fmtPos = this.format == Format.ASCII ? FMT_COORDS_8 : FMT_COORDS;

            String strPos = String.format(fmtPos, pos.x, pos.y, pos.z);
            String strRegion = String.format(fmtRegion, rx, rz);
            String strChunk = String.format(fmtChunk, cx, cz);

            if (this.printDimension)
            {
                dump.addData(data.name, data.dimension, strRegion, strChunk, strPos);
            }
            else
            {
                dump.addData(data.name, strRegion, strChunk, strPos);
            }
        }
    }

    protected static class LocationData
    {
        private final String name;
        private final String dimension;
        private final Vec3 pos;

        private LocationData(String name, String dimension, Vec3 pos)
        {
            this.name = name;
            this.dimension = dimension;
            this.pos = pos;
        }

        protected static LocationData of(String name, String dimension, Vec3 pos)
        {
            return new LocationData(name, dimension, pos);
        }
    }

    public enum LocateType
    {
        BLOCK       ("block",        "blocks",           () -> ForgeRegistries.BLOCKS, LocateBlocks::new),
        ENTITY      ("entity",       "entities",         () -> ForgeRegistries.ENTITIES, LocateEntities::new),
        BLOCK_ENTITY("block-entity", "block_entities",   () -> ForgeRegistries.BLOCK_ENTITIES, LocateBlockEntities::new);

        private final String argument;
        private final String plural;
        private final Supplier<IForgeRegistry<?>> registrySupplier;
        private final IChunkProcessorFactory chunkProcessorFactory;

        LocateType(String argument,
                   String plural,
                   Supplier<IForgeRegistry<?>> registrySupplier,
                   IChunkProcessorFactory chunkProcessorFactory)
        {
            this.argument = argument;
            this.plural = plural;
            this.registrySupplier = registrySupplier;
            this.chunkProcessorFactory = chunkProcessorFactory;
        }

        public String getArgument()
        {
            return this.argument;
        }

        public String getPlural()
        {
            return this.plural;
        }

        public Supplier<IForgeRegistry<?>> getRegistrySupplier()
        {
            return this.registrySupplier;
        }

        public LocateBase createChunkProcessor(DataDump.Format format, List<String> filterStrings) throws CommandSyntaxException
        {
            return this.chunkProcessorFactory.createChunkProcessor(format, filterStrings);
        }
    }

    public interface IChunkProcessorFactory
    {
        LocateBase createChunkProcessor(DataDump.Format format, List<String> filterStrings) throws CommandSyntaxException;
    }
}
