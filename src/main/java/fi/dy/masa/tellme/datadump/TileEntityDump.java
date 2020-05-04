package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityDump
{
    public static List<String> getFormattedTileEntityDump(DataDump.Format format)
    {
        DataDump tileEntityDump = new DataDump(3, format);

        try
        {
            for (Map.Entry<ResourceLocation, TileEntityType<?>> entry : ForgeRegistries.TILE_ENTITIES.getEntries())
            {
                String id = entry.getKey().toString();
                TileEntityType<?> type = entry.getValue();
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
