package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import fi.dy.masa.tellme.TellMe;

public class TileEntityDump extends DataDump
{
    private static final Field field_nameToClassMap = ReflectionHelper.findField(TileEntity.class, "field_145855_i", "nameToClassMap");

    private TileEntityDump(Format format)
    {
        super(2, format);
    }

    public static List<String> getFormattedTileEntityDump(Format format)
    {
        TileEntityDump tileEntityDump = new TileEntityDump(format);
        try
        {
            @SuppressWarnings("unchecked")
            Iterator<Map.Entry<String, Class<? extends TileEntity>>> iter = ((Map<String, Class<? extends TileEntity>>) field_nameToClassMap.get(null)).entrySet().iterator();

            while (iter.hasNext())
            {
                Map.Entry<String, Class<? extends TileEntity>> entry = iter.next();
                tileEntityDump.addData(entry.getValue().getName(), entry.getKey());
            }

            tileEntityDump.addTitle("Class", "Name");
            tileEntityDump.setUseColumnSeparator(true);
        }
        catch (Exception e)
        {
            TellMe.logger.warn("Failed to dump the TileEntity map");
        }

        return tileEntityDump.getLines();
    }
}
