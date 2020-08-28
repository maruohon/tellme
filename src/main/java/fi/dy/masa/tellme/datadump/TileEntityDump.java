package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.registries.ForgeRegistries;
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
            for (Map.Entry<RegistryKey<TileEntityType<?>>, TileEntityType<?>> entry : ForgeRegistries.TILE_ENTITIES.getEntries())
            {
                TileEntityType<?> type = entry.getValue();
                String id = type.getRegistryName().toString();
                TileEntity te = type.create();
                Class <? extends TileEntity> clazz = te.getClass();
                tileEntityDump.addData(id, clazz.getName(), ITickableTileEntity.class.isAssignableFrom(clazz) ? "yes" : "-");
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
