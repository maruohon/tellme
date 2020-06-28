package fi.dy.masa.tellme.datadump;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.mixin.IMixinServerWorld;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class DimensionDump
{
    public static List<String> getFormattedDimensionDump(Format format, boolean verbose)
    {
        DataDump dump = new DataDump(verbose ? 12 : 2, format);

        Registry<DimensionType> registry = RegistryTracker.create().getDimensionTypeRegistry();

        for (DimensionType dim : registry)
        {
            String dimId = registry.getId(dim).toString();
            String natural = String.valueOf(dim.isNatural());

            if (verbose)
            {
                String bedWorks = String.valueOf(dim.isBedWorking());
                String ceiling = String.valueOf(dim.hasCeiling());
                String dragon = String.valueOf(dim.hasEnderDragonFight());
                String raids = String.valueOf(dim.hasRaids());
                String skyLight = String.valueOf(dim.hasSkyLight());
                String logicalHeight = String.valueOf(dim.getLogicalHeight());
                String piglinSafe = String.valueOf(dim.isPiglinSafe());
                String respawnAnchor = String.valueOf(dim.isRespawnAnchorWorking());
                String shrunk = String.valueOf(dim.isShrunk());
                String ultrawarm = String.valueOf(dim.isUltrawarm());

                dump.addData(dimId, natural, bedWorks, ceiling, dragon, logicalHeight, piglinSafe, raids, respawnAnchor, shrunk, skyLight, ultrawarm);
            }
            else
            {
                dump.addData(dimId, natural);
            }
        }

        if (verbose)
        {
            dump.addTitle("ID", "Natural", "Bed works", "Ceiling", "Dragon", "Height", "Piglin safe", "Raids", "Resp. Anchor", "Shrunk", "Sky Light", "Ultra Warm");
            dump.setColumnAlignment(1, Alignment.RIGHT); // natural
            dump.setColumnAlignment(2, Alignment.RIGHT); // bed
            dump.setColumnAlignment(3, Alignment.RIGHT); // ceiling
            dump.setColumnAlignment(4, Alignment.RIGHT); // dragon
            dump.setColumnProperties(5, Alignment.RIGHT, true); // height
            dump.setColumnAlignment(6, Alignment.RIGHT); // piglin
            dump.setColumnAlignment(7, Alignment.RIGHT); // raids
            dump.setColumnAlignment(8, Alignment.RIGHT); // respawn anchor
            dump.setColumnAlignment(9, Alignment.RIGHT); // shrunk
            dump.setColumnAlignment(10, Alignment.RIGHT); // sky light
            dump.setColumnAlignment(11, Alignment.RIGHT); // ultra warm
        }
        else
        {
            dump.addTitle("ID", "Natural");
        }

        dump.setSort(false);

        return dump.getLines();
    }

    public static List<String> getLoadedDimensions(Format format, MinecraftServer server)
    {
        DataDump dimensionDump = new DataDump(4, format);
        dimensionDump.setSort(false);

        if (server != null)
        {
            for (World world : server.getWorlds())
            {
                String dimId = world.getDimensionRegistryKey().getValue().toString();
                String loadedChunks = String.valueOf(WorldUtils.getLoadedChunkCount(world));
                String entityCount = String.valueOf(((IMixinServerWorld) world).tellmeGetEntitiesByIdMap().size());
                String playerCount = String.valueOf(world.getPlayers().size());

                dimensionDump.addData(dimId, loadedChunks, entityCount, playerCount);
            }
        }

        dimensionDump.addTitle("ID", "Loaded Chunks", "Entities", "Players");
        dimensionDump.setColumnProperties(1, Alignment.RIGHT, true); // loaded chunks
        dimensionDump.setColumnProperties(2, Alignment.RIGHT, true); // entity count
        dimensionDump.setColumnProperties(3, Alignment.RIGHT, true); // players

        return dimensionDump.getLines();
    }
}
