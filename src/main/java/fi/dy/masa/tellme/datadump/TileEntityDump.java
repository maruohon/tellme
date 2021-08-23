package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class TileEntityDump
{
    public static List<String> getFormattedTileEntityDump(DataDump.Format format)
    {
        DataDump tileEntityDump = new DataDump(1, format);

        for (Identifier id : Registry.BLOCK_ENTITY_TYPE.getIds())
        {
            /*
            try
            {
                BlockEntityType<?> type = Registry.BLOCK_ENTITY_TYPE.get(id);
                BlockEntity be = type.instantiate(BlockPos.ORIGIN, air);
                tileEntityDump.addData(id.toString(), clazz.getName(), Tickable.class.isAssignableFrom(clazz) ? "yes" : "-");
            }
            catch (Exception e)
            {
                TellMe.logger.warn("Failed to dump the TileEntity map");
            }
            */

            tileEntityDump.addData(id.toString());
        }

        tileEntityDump.addTitle("Registry name");
        //tileEntityDump.addTitle("Registry name", "Class", "Ticking?");
        //tileEntityDump.setColumnAlignment(2, Alignment.RIGHT);

        return tileEntityDump.getLines();
    }
}
