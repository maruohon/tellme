package fi.dy.masa.tellme.datadump;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;

public class ChunkDump
{
    public static List<String> getFormattedChunkDump(Format format, @Nullable Integer dimension)
    {
        Integer[] ids;

        if (dimension != null)
        {
            ids = new Integer[] { dimension };
        }
        else
        {
            ids = DimensionManager.getIDs(); // only loaded dimensions
        }

        DataDump chunkDump = new DataDump(4, format);

        for (int i = 0; i < ids.length; i++)
        {
            Integer id = ids[i];
            World world = DimensionManager.getWorld(id);

            if (world != null)
            {
                String dimId = ids[i].toString();
                Collection<Chunk> chunks = TellMe.proxy.getLoadedChunks(world);

                for (Chunk chunk : chunks)
                {
                    String cx = String.valueOf(chunk.x);
                    String cz = String.valueOf(chunk.z);
                    int count = 0;

                    for (int l = 0; l < chunk.getEntityLists().length; l++)
                    {
                        count += chunk.getEntityLists()[l].size();
                    }

                    String entityCount = String.valueOf(count);

                    chunkDump.addData(dimId, cx, cz, entityCount);
                }
            }
        }

        chunkDump.addTitle("Dim ID", "CX", "CZ", "Entities");

        chunkDump.setColumnProperties(0, Alignment.RIGHT, true); // dim ID
        chunkDump.setColumnProperties(1, Alignment.RIGHT, true); // Chunk X
        chunkDump.setColumnProperties(2, Alignment.RIGHT, true); // Chunk Z
        chunkDump.setColumnProperties(3, Alignment.RIGHT, true); // entity count

        chunkDump.setUseColumnSeparator(true);

        return chunkDump.getLines();
    }
}
