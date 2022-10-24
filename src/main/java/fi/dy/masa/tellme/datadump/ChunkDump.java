package fi.dy.masa.tellme.datadump;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.WorldUtils;

public class ChunkDump
{
    public static List<String> getFormattedChunkDump(Format format, @Nullable Integer dimension, MinecraftServer server)
    {
        DataDump chunkDump = new DataDump(4, format);

        for (World world : server.worlds)
        {
            if (world != null)
            {
                String dimId = malilib.util.game.WorldUtils.getDimensionIdAsString(world);
                Collection<Chunk> chunks = WorldUtils.getLoadedChunks(world);

                for (Chunk chunk : chunks)
                {
                    int count = 0;

                    for (int l = 0; l < chunk.getEntityLists().length; l++)
                    {
                        count += chunk.getEntityLists()[l].size();
                    }

                    String entityCount = String.valueOf(count);

                    chunkDump.addData(  dimId,
                                        String.format("%4d, %4d", chunk.x, chunk.z),
                                        String.format("%5d, %5d", chunk.x << 4, chunk.z << 4),
                                        entityCount);
                }
            }
        }

        chunkDump.addTitle("Dim ID", "Chunk", "Block pos", "Entities");

        chunkDump.setColumnProperties(0, Alignment.RIGHT, true); // dim ID
        //chunkDump.setColumnProperties(1, Alignment.RIGHT, false); // Chunk
        //chunkDump.setColumnProperties(2, Alignment.RIGHT, false); // Block pos
        chunkDump.setColumnProperties(3, Alignment.RIGHT, true); // entity count

        chunkDump.setUseColumnSeparator(true);

        return chunkDump.getLines();
    }
}
