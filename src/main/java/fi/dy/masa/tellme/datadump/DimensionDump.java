package fi.dy.masa.tellme.datadump;

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;

import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.mixin.IMixinDimensionType;

public class DimensionDump
{
    public static List<String> getFormattedDimensionDump(Format format, MinecraftServer server)
    {
        DataDump dimensionDump = new DataDump(6, format);
        dimensionDump.setSort(false);

        for (WorldServer world : server.worlds)
        {
            DimensionType type = world.provider.getDimensionType();
            String typeId = String.valueOf(type.getId());
            String name = type.getName();
            String shouldLoadSpawn = String.valueOf(type.getId() == 0);
            String worldProviderClass = ((IMixinDimensionType) (Object) type).getClazz().getName();
            String currentlyLoaded = String.valueOf(world != null);
            String loadedChunks = String.valueOf(world.getChunkProvider().getLoadedChunks().size());

            dimensionDump.addData(typeId, name, shouldLoadSpawn, currentlyLoaded, loadedChunks, worldProviderClass);
        }

        dimensionDump.addTitle("DimensionType ID", "Name", "shouldLoadSpawn", "Currently loaded", "Loaded chunks", "WorldProvider class");

        dimensionDump.setColumnProperties(0, Alignment.RIGHT, true); // type ID
        dimensionDump.setColumnAlignment(2, Alignment.RIGHT); // shouldLoadSpawn
        dimensionDump.setColumnAlignment(3, Alignment.RIGHT); // currentlyLoaded
        dimensionDump.setColumnProperties(4, Alignment.RIGHT, true); // loaded chunks

        dimensionDump.setUseColumnSeparator(true);

        return dimensionDump.getLines();
    }
}
