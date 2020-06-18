package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.mixin.IMixinServerWorld;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class DimensionDump
{
    @SuppressWarnings("deprecation")
    public static List<String> getFormattedDimensionDump(Format format, @Nullable MinecraftServer server)
    {
        DataDump dimensionDump = new DataDump(5, format);

        if (server != null)
        {
            World overworld = server.getWorld(DimensionType.OVERWORLD);

            for (DimensionType dim : Registry.DIMENSION)
            {
                String dimId = Registry.DIMENSION.getId(dim).toString();
                String typeId = String.valueOf(dim.getRawId());
                String hasSkylight = String.valueOf(dim.hasSkyLight());

                World world = server.getWorld(dim);
                boolean loaded = false;
                String dimensionClass;

                if (world != null)
                {
                    loaded = true;
                    dimensionClass = world.getDimension().getClass().getName();
                }
                else
                {
                    dimensionClass = dim.create(overworld).getClass().getName();
                }

                String currentlyLoaded = String.valueOf(loaded);

                dimensionDump.addData(dimId, typeId, hasSkylight, currentlyLoaded, dimensionClass);
            }
        }

        dimensionDump.addTitle("ID", "Raw ID", "Has Sky Light", "Loaded?", "Dimension class");
        dimensionDump.setColumnProperties(1, Alignment.RIGHT, true); // type ID
        dimensionDump.setColumnAlignment(2, Alignment.RIGHT); // has sky light
        dimensionDump.setColumnAlignment(3, Alignment.RIGHT); // is vanilla
        dimensionDump.setColumnAlignment(4, Alignment.RIGHT); // loaded?
        dimensionDump.setSort(false);

        return dimensionDump.getLines();
    }

    public static List<String> getLoadedDimensions(Format format, MinecraftServer server)
    {
        DataDump dimensionDump = new DataDump(6, format);
        dimensionDump.setSort(false);

        if (server != null)
        {
            for (DimensionType dim : Registry.DIMENSION)
            {
                String dimId = Registry.DIMENSION.getId(dim).toString();
                String typeId = String.valueOf(dim.getRawId());

                ServerWorld world = server.getWorld(dim);
                String dimensionClass;

                if (world != null)
                {
                    String loadedChunks = String.valueOf(WorldUtils.getLoadedChunkCount(world));
                    String entityCount = String.valueOf(((IMixinServerWorld) world).getEntitiesByIdMap().size());
                    String playerCount = String.valueOf(world.getPlayers().size());
                    dimensionClass = world.getDimension().getClass().getName();

                    dimensionDump.addData(dimId, typeId, dimensionClass, loadedChunks, entityCount, playerCount);
                }
            }
        }

        dimensionDump.addTitle("ID", "Raw ID", "Dimension class", "Loaded Chunks", "Entities", "Players");
        dimensionDump.setColumnProperties(1, Alignment.RIGHT, true); // type ID
        dimensionDump.setColumnProperties(3, Alignment.RIGHT, true); // loaded chunks
        dimensionDump.setColumnProperties(4, Alignment.RIGHT, true); // entity count
        dimensionDump.setColumnProperties(5, Alignment.RIGHT, true); // players

        return dimensionDump.getLines();
    }
}
