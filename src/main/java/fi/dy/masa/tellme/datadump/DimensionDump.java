package fi.dy.masa.tellme.datadump;

import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class DimensionDump extends DataDump
{
    private static final Field field_worldProvider = ReflectionHelper.findField(DimensionType.class, "field_186077_g", "clazz");

    private DimensionDump()
    {
        super(6);

        this.setSort(false);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getFormattedDimensionDump()
    {
        DimensionDump dimensionDump = new DimensionDump();
        Integer[] ids = DimensionManager.getStaticDimensionIDs();

        for (int i = 0; i < ids.length; i++)
        {
            DimensionType type = DimensionManager.getProviderType(ids[i]);

            if (type == null)
            {
                continue;
            }

            String dimId = ids[i].toString();
            String typeId = String.valueOf(type.getId());
            String name = type.getName();
            String shouldLoadSpawn = String.valueOf(type.shouldLoadSpawn());
            String worldProviderClass;
            String currentlyLoaded = String.valueOf(DimensionManager.getWorld(ids[i]) != null);

            try
            {
                worldProviderClass = ((Class<? extends WorldProvider>) field_worldProvider.get(type)).getSimpleName();
            }
            catch (Exception e)
            {
                worldProviderClass = "ERROR";
            }

            dimensionDump.addData(dimId, typeId, name, shouldLoadSpawn, worldProviderClass, currentlyLoaded);
        }

        dimensionDump.addTitle("ID", "DimensionType ID", "Name", "shouldLoadSpawn", "WorldProvider class", "Currently loaded");
        dimensionDump.setColumnAlignment(0, Alignment.RIGHT); // dim ID
        dimensionDump.setColumnAlignment(1, Alignment.RIGHT); // type ID
        dimensionDump.setColumnAlignment(3, Alignment.RIGHT); // shouldLoadSpawn
        dimensionDump.setColumnAlignment(5, Alignment.RIGHT); // currentlyLoaded
        dimensionDump.setUseColumnSeparator(true);

        return dimensionDump.getLines();
    }
}
