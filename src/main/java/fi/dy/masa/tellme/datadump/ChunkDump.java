package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class ChunkDump
{
    public static List<String> getFormattedChunkDump(DataDump.Format format, MinecraftServer server, @Nullable Level worldIn)
    {
        return getFormattedChunkDump(format, server, worldIn, null, null);
    }

    public static List<String> getFormattedChunkDump(DataDump.Format format, @Nullable MinecraftServer server, @Nullable Level worldIn, @Nullable BlockPos minPos, @Nullable BlockPos maxPos)
    {
        DataDump chunkDump = new DataDump(4, format);

        if (server != null)
        {
            List<Level> worlds = new ArrayList<>();

            if (worldIn != null)
            {
                worlds.add(worldIn);
            }
            else
            {
                for (Level world : server.getAllLevels())
                {
                    worlds.add(world);
                }
            }

            final int minCX = minPos != null ? minPos.getX() >> 4 : Integer.MIN_VALUE;
            final int minCZ = minPos != null ? minPos.getZ() >> 4 : Integer.MIN_VALUE;
            final int maxCX = maxPos != null ? maxPos.getX() >> 4 : Integer.MAX_VALUE;
            final int maxCZ = maxPos != null ? maxPos.getZ() >> 4 : Integer.MAX_VALUE;
            int chunkCount = 0;

            for (Level world : worlds)
            {
                if (world != null)
                {
                    String dimId = WorldUtils.getDimensionId(world);
                    Collection<LevelChunk> chunks = TellMe.dataProvider.getLoadedChunks(world);

                    for (LevelChunk chunk : chunks)
                    {
                        ChunkPos cp = chunk.getPos();

                        if (cp.x < minCX || cp.x > maxCX || cp.z < minCZ || cp.z > maxCZ)
                        {
                            continue;
                        }

                        ++chunkCount;
                        int count = 0;

                        for (int l = 0; l < chunk.getEntitySections().length; l++)
                        {
                            count += chunk.getEntitySections()[l].size();
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
        chunkDump.setColumnProperties(3, DataDump.Alignment.RIGHT, true); // entity count


        return chunkDump.getLines();
    }
}
