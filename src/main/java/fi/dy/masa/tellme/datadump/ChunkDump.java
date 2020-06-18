package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ChunkDump
{
    public static List<String> getFormattedChunkDump(Format format, MinecraftServer server, @Nullable DimensionType dimension)
    {
        return getFormattedChunkDump(format, server, dimension, null, null);
    }

    @SuppressWarnings("deprecation")
    public static List<String> getFormattedChunkDump(Format format, @Nullable MinecraftServer server, @Nullable DimensionType dimension, @Nullable BlockPos minPos, @Nullable BlockPos maxPos)
    {
        DataDump chunkDump = new DataDump(4, format);

        if (server != null)
        {
            List<DimensionType> dims = new ArrayList<>();

            if (dimension != null)
            {
                dims.add(dimension);
            }
            else
            {
                for (DimensionType dim : Registry.DIMENSION)
                {
                    dims.add(dim);
                }
            }

            final int minCX = minPos != null ? minPos.getX() >> 4 : Integer.MIN_VALUE;
            final int minCZ = minPos != null ? minPos.getZ() >> 4 : Integer.MIN_VALUE;
            final int maxCX = maxPos != null ? maxPos.getX() >> 4 : Integer.MAX_VALUE;
            final int maxCZ = maxPos != null ? maxPos.getZ() >> 4 : Integer.MAX_VALUE;
            int chunkCount = 0;

            for (DimensionType dim : dims)
            {
                World world = server.getWorld(dim);

                if (world != null)
                {
                    String dimId = Registry.DIMENSION.getId(dim).toString();
                    Collection<WorldChunk> chunks = TellMe.dataProvider.getLoadedChunks(world);

                    for (WorldChunk chunk : chunks)
                    {
                        ChunkPos cp = chunk.getPos();

                        if (cp.x < minCX || cp.x > maxCX || cp.z < minCZ || cp.z > maxCZ)
                        {
                            continue;
                        }

                        ++chunkCount;
                        int count = 0;

                        for (int l = 0; l < chunk.getEntitySectionArray().length; l++)
                        {
                            count += chunk.getEntitySectionArray()[l].size();
                        }

                        String entityCount = String.valueOf(count);

                        chunkDump.addData(  dimId,
                                            String.format("%4d, %4d", cp.x, cp.z),
                                            String.format("%5d, %5d", cp.x << 4, cp.z << 4),
                                            entityCount);
                    }
                }
            }

            chunkDump.addFooter("Total loaded chunks in the requested area: " + chunkCount);
        }

        chunkDump.addTitle("Dim ID", "Chunk", "Block pos", "Entities");
        //chunkDump.setColumnProperties(1, Alignment.RIGHT, false); // Chunk
        //chunkDump.setColumnProperties(2, Alignment.RIGHT, false); // Block pos
        chunkDump.setColumnProperties(3, Alignment.RIGHT, true); // entity count


        return chunkDump.getLines();
    }
}
