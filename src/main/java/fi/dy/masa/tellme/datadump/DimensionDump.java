package fi.dy.masa.tellme.datadump;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import fi.dy.masa.tellme.util.WorldUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class DimensionDump
{
    public static List<String> getFormattedDimensionDump(Format format, @Nullable MinecraftServer server, boolean verbose)
    {
        DataDump dump = new DataDump(verbose ? 12 : 3, format);

        if (server != null)
        {
            for (World world : server.getWorlds())
            {
                DimensionType dim = world.func_230315_m_();
                String dimId = WorldUtils.getDimensionId(world);
                String natural = String.valueOf(dim.func_236043_f_());
                String coordScale = String.valueOf(dim.func_242724_f());

                if (verbose)
                {
                    String bedWorks = String.valueOf(dim.func_241510_j_());
                    String ceiling = String.valueOf(dim.func_236037_d_());
                    String dragon = String.valueOf(dim.func_236046_h_());
                    String raids = String.valueOf(dim.func_241512_l_());
                    String skyLight = String.valueOf(dim.hasSkyLight());
                    String logicalHeight = String.valueOf(dim.func_241513_m_());
                    String piglinSafe = String.valueOf(dim.func_241509_i_());
                    String respawnAnchor = String.valueOf(dim.func_241511_k_());
                    String ultrawarm = String.valueOf(dim.func_236040_e_());

                    dump.addData(dimId, natural, coordScale, bedWorks, ceiling, dragon, logicalHeight, piglinSafe, raids, respawnAnchor, skyLight, ultrawarm);
                }
                else
                {
                    dump.addData(dimId, natural, coordScale);
                }
            }
        }

        if (verbose)
        {
            dump.addTitle("ID", "Natural", "Coord Scale", "Bed works", "Ceiling", "Dragon", "Height", "Piglin safe", "Raids", "Resp. Anchor", "Sky Light", "Ultra Warm");
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
                String entityCount = String.valueOf(world.getEntities().count());
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
