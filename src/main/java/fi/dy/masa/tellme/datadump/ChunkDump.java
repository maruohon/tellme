package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ChunkDump
{
    public static List<String> getFormattedChunkDump(Format format, @Nullable DimensionType dimension)
    {
        return getFormattedChunkDump(format, dimension, null, null);
    }

    @SuppressWarnings("deprecation")
    public static List<String> getFormattedChunkDump(Format format, @Nullable DimensionType dimension, @Nullable BlockPos minPos, @Nullable BlockPos maxPos)
    {
        List<DimensionType> dims = new ArrayList<>();

        if (dimension != null)
        {
            dims.add(dimension);
        }
        else
        {
            for (DimensionType dim : Registry.DIMENSION_TYPE)
            {
                dims.add(dim);
            }
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        DataDump chunkDump = new DataDump(4, format);
        final int minCX = minPos != null ? minPos.getX() >> 4 : Integer.MIN_VALUE;
        final int minCZ = minPos != null ? minPos.getZ() >> 4 : Integer.MIN_VALUE;
        final int maxCX = maxPos != null ? maxPos.getX() >> 4 : Integer.MAX_VALUE;
        final int maxCZ = maxPos != null ? maxPos.getZ() >> 4 : Integer.MAX_VALUE;
        int chunkCount = 0;

        for (DimensionType dim : dims)
        {
            World world = DimensionManager.getWorld(server, dim, false, false);

            if (world != null)
            {
                String dimId = dim.getRegistryName().toString();
                Collection<Chunk> chunks = TellMe.dataProvider.getLoadedChunks(world);

                for (Chunk chunk : chunks)
                {
                    ChunkPos cp = chunk.getPos();

                    if (cp.x < minCX || cp.x > maxCX || cp.z < minCZ || cp.z > maxCZ)
                    {
                        continue;
                    }

                    ++chunkCount;
                    int count = 0;

                    for (int l = 0; l < chunk.getEntityLists().length; l++)
                    {
                        count += chunk.getEntityLists()[l].size();
                    }

                    String entityCount = String.valueOf(count);
                    ChunkPos cpos = chunk.getPos();

                    chunkDump.addData(  dimId,
                                        String.format("%4d, %4d", cpos.x, cpos.z),
                                        String.format("%5d, %5d", cpos.x << 4, cpos.z << 4),
                                        entityCount);
                }
            }
        }

        chunkDump.addTitle("Dim ID", "Chunk", "Block pos", "Entities");
        //chunkDump.setColumnProperties(1, Alignment.RIGHT, false); // Chunk
        //chunkDump.setColumnProperties(2, Alignment.RIGHT, false); // Block pos
        chunkDump.setColumnProperties(3, Alignment.RIGHT, true); // entity count

        chunkDump.addFooter("Total loaded chunks in the requested area: " + chunkCount);

        return chunkDump.getLines();
    }
}
