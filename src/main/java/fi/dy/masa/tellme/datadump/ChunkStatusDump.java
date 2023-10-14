package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.ChunkStatus;

import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class ChunkStatusDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(4, format);

        for (Identifier id : Registries.CHUNK_STATUS.getIds())
        {
            ChunkStatus val = Registries.CHUNK_STATUS.get(id);
            String index = String.valueOf(val.getIndex());
            String type = val.getChunkType().name();
            String taskRange = String.valueOf(val.getTaskMargin());

            dump.addData(index, id.toString(), type, taskRange);
        }

        dump.addTitle("Index", "Registry name", "Type", "Task Margin");

        return dump.getLines();
    }
}
