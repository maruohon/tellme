package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ChunkStatusDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(4, format);

        for (Map.Entry<ResourceKey<ChunkStatus>, ChunkStatus> entry : ForgeRegistries.CHUNK_STATUS.getEntries())
        {
            ChunkStatus val = entry.getValue();
            String ordinal = String.valueOf(val.getIndex());
            String regName = val.getRegistryName().toString();
            String type = val.getChunkType().name();
            String taskRange = String.valueOf(val.getRange());

            dump.addData(ordinal, regName, type, taskRange);
        }

        dump.addTitle("Ordinal", "Registry name", "Type", "Task Range");

        return dump.getLines();
    }
}
