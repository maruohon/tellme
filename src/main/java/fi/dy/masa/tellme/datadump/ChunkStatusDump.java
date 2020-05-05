package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.ChunkStatus;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class ChunkStatusDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(4, format);

        for (Map.Entry<ResourceLocation, ChunkStatus> entry : ForgeRegistries.CHUNK_STATUS.getEntries())
        {
            ChunkStatus val = entry.getValue();
            String ordinal = String.valueOf(val.ordinal());
            String regName = val.getRegistryName().toString();
            String type = val.getType().name();
            String taskRange = String.valueOf(val.getTaskRange());

            dump.addData(ordinal, regName, type, taskRange);
        }

        dump.addTitle("Ordinal", "Registry name", "Type", "Task Range");

        return dump.getLines();
    }
}
