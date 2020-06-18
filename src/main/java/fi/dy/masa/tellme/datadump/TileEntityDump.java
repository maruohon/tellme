package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.registry.Registry;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;

public class TileEntityDump
{
    public static List<String> getFormattedTileEntityDump(DataDump.Format format)
    {
        DataDump tileEntityDump = new DataDump(3, format);

        try
        {
            for (Identifier id : Registry.BLOCK_ENTITY.getIds())
            {
                BlockEntityType<?> type = Registry.BLOCK_ENTITY.get(id);
                BlockEntity be = type.instantiate();
                Class <? extends BlockEntity> clazz = be.getClass();
                tileEntityDump.addData(id.toString(), clazz.getName(), Tickable.class.isAssignableFrom(clazz) ? "yes" : "-");
            }

            tileEntityDump.addTitle("Registry name", "Class", "Ticking?");
            tileEntityDump.setColumnAlignment(2, Alignment.RIGHT);
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Failed to dump the TileEntity map");
        }

        return tileEntityDump.getLines();
    }
}
