package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import fi.dy.masa.tellme.mixin.IMixinWorld;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class DimensionDump
{
    public static List<String> getFormattedDimensionDump(Format format, @Nullable MinecraftServer server, boolean verbose)
    {
        DataDump dump = new DataDump(verbose ? 13 : 3, format);

        if (server != null)
        {
            for (World world : server.getWorlds())
            {
                DimensionType dim = world.getDimension();
                String dimId = WorldUtils.getDimensionId(world);
                String natural = String.valueOf(dim.natural());
                String coordScale = String.valueOf(dim.coordinateScale());

                if (verbose)
                {
                    String bedWorks = String.valueOf(dim.bedWorks());
                    String ceiling = String.valueOf(dim.hasCeiling());
                    String raids = String.valueOf(dim.hasRaids());
                    String skyLight = String.valueOf(dim.hasSkyLight());
                    String minY = String.valueOf(dim.minY());
                    String height = String.valueOf(dim.height());
                    String logicalHeight = String.valueOf(dim.logicalHeight());
                    String piglinSafe = String.valueOf(dim.piglinSafe());
                    String respawnAnchor = String.valueOf(dim.respawnAnchorWorks());
                    String ultrawarm = String.valueOf(dim.ultrawarm());

                    dump.addData(dimId, natural, coordScale, bedWorks, ceiling, minY, height, logicalHeight, piglinSafe, raids, respawnAnchor, skyLight, ultrawarm);
                }
                else
                {
                    dump.addData(dimId, natural, coordScale);
                }
            }
        }

        if (verbose)
        {
            dump.addTitle("ID", "Natural", "Coord Scale", "Bed works", "Ceiling", "minY", "Height", "Logical Height", "Piglin safe", "Raids", "Resp. Anchor", "Sky Light", "Ultra Warm");
            dump.setColumnAlignment(1, Alignment.RIGHT); // natural
            dump.setColumnProperties(2, Alignment.RIGHT, true); // coord scale
            dump.setColumnAlignment(3, Alignment.RIGHT); // bed
            dump.setColumnAlignment(4, Alignment.RIGHT); // ceiling
            dump.setColumnProperties(5, Alignment.RIGHT, true); // min y
            dump.setColumnProperties(6, Alignment.RIGHT, true); // height
            dump.setColumnProperties(7, Alignment.RIGHT, true); // logical height
            dump.setColumnAlignment(8, Alignment.RIGHT); // piglin
            dump.setColumnAlignment(9, Alignment.RIGHT); // raids
            dump.setColumnAlignment(10, Alignment.RIGHT); // respawn anchor
            dump.setColumnAlignment(11, Alignment.RIGHT); // sky light
            dump.setColumnAlignment(12, Alignment.RIGHT); // ultra warm
        }
        else
        {
            dump.addTitle("ID", "Natural", "Coord Scale");
        }

        dump.setSort(false);

        return dump.getLines();
    }

    public static List<String> getLoadedDimensions(Format format, @Nullable MinecraftServer server)
    {
        DataDump dimensionDump = new DataDump(4, format);
        dimensionDump.setSort(false);

        if (server != null)
        {
            for (ServerWorld world : server.getWorlds())
            {
                String dimId = WorldUtils.getDimensionId(world);
                String loadedChunks = String.valueOf(WorldUtils.getLoadedChunkCount(world));
                long entityCount = StreamSupport.stream(((IMixinWorld) world).tellme_getEntityLookup().iterate().spliterator(), false).count();
                String entityCountStr = String.valueOf(entityCount);
                String playerCount = String.valueOf(world.getPlayers().size());

                dimensionDump.addData(dimId, loadedChunks, entityCountStr, playerCount);
            }
        }

        dimensionDump.addTitle("ID", "Loaded Chunks", "Entities", "Players");
        dimensionDump.setColumnProperties(1, Alignment.RIGHT, true); // loaded chunks
        dimensionDump.setColumnProperties(2, Alignment.RIGHT, true); // entity count
        dimensionDump.setColumnProperties(3, Alignment.RIGHT, true); // players

        return dimensionDump.getLines();
    }
}
