package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.WorldUtils;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class DimensionDump
{
    @SuppressWarnings("deprecation")
    public static List<String> getFormattedDimensionDump(Format format)
    {
        DataDump dimensionDump = new DataDump(6, format);
        dimensionDump.setSort(false);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        World overworld = DimensionManager.getWorld(server, DimensionType.OVERWORLD, false, false);

        for (DimensionType dim : Registry.DIMENSION_TYPE)
        {
            String dimId = dim.getRegistryName().toString();
            String typeId = String.valueOf(dim.getId());
            String hasSkylight = String.valueOf(dim.hasSkyLight());
            String isVanilla = String.valueOf(dim.isVanilla());

            World world = DimensionManager.getWorld(server, dim, false, false);
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

            dimensionDump.addData(dimId, typeId, hasSkylight, isVanilla, currentlyLoaded, dimensionClass);
        }

        dimensionDump.addTitle("ID", "Raw ID", "Has Sky Light", "Is Vanilla", "Loaded?", "Dimension class");
        dimensionDump.setColumnProperties(1, Alignment.RIGHT, true); // type ID
        dimensionDump.setColumnAlignment(2, Alignment.RIGHT); // has sky light
        dimensionDump.setColumnAlignment(3, Alignment.RIGHT); // is vanilla
        dimensionDump.setColumnAlignment(4, Alignment.RIGHT); // loaded?

        return dimensionDump.getLines();
    }

    @SuppressWarnings("deprecation")
    public static List<String> getLoadedDimensions(Format format)
    {
        DataDump dimensionDump = new DataDump(6, format);
        dimensionDump.setSort(false);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        for (Map.Entry<DimensionType, ServerWorld> entry : server.forgeGetWorldMap().entrySet())
        {
            DimensionType dim = entry.getKey();
            ServerWorld world = entry.getValue();
            String dimId = dim.getRegistryName().toString();
            String typeId = String.valueOf(dim.getId());
            String dimensionClass = world.getDimension().getClass().getName();
            // TODO 1.14+: Reflect the map and use size() ?
            String loadedChunks = String.valueOf(WorldUtils.getLoadedChunkCount(world));
            String entityCount = String.valueOf(world.getEntities().count());
            String playerCount = String.valueOf(world.getPlayers().size());

            dimensionDump.addData(dimId, typeId, dimensionClass, loadedChunks, entityCount, playerCount);
        }

        dimensionDump.addTitle("ID", "Raw ID", "Dimension class", "Loaded Chunks", "Entities", "Players");
        dimensionDump.setColumnProperties(1, Alignment.RIGHT, true); // type ID
        dimensionDump.setColumnProperties(3, Alignment.RIGHT, true); // loaded chunks
        dimensionDump.setColumnProperties(4, Alignment.RIGHT, true); // entity count
        dimensionDump.setColumnProperties(5, Alignment.RIGHT, true); // players

        return dimensionDump.getLines();
    }
}
