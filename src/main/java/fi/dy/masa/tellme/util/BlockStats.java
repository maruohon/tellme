package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import fi.dy.masa.tellme.TellMe;

public class BlockStats
{
    private HashMap<String, BlockInfo> blockStats;
    private ArrayList<String> blockStatLines;
    private int longestName = 0;
    private int longestDisplayName = 0;

    public class BlockInfo implements Comparable<BlockInfo>
    {
        public String name;
        public String displayName;
        public int id;
        public int meta;
        public int count;
        public int countTE;

        public BlockInfo(String name, String displayName, int id, int meta, int count, int countTE)
        {
            this.name = name;
            this.displayName = displayName;
            this.id = id;
            this.meta = meta;
            this.count = count;
            this.countTE = countTE;
        }

        public int compareTo(BlockInfo blockInfo)
        {
            if (blockInfo == null)
            {
                throw new NullPointerException();
            }

            if (this.id != blockInfo.id)
            {
                return this.id - blockInfo.id;
            }

            if (this.meta != blockInfo.meta)
            {
                return this.meta - blockInfo.meta;
            }

            return 0;
        }
    }

    public BlockStats()
    {
        this.blockStats = new HashMap<String, BlockInfo>();
        this.blockStatLines = new ArrayList<String>();
    }

    public boolean checkChunksAreLoaded(int dim, int x1, int z1, int x2, int z2)
    {
        // TODO Do we need/want this anyway?
        return true;
    }

    private boolean areCoordinatesValid(int x1, int y1, int z1, int x2, int y2, int z2) throws CommandException
    {
        if (y1 < 0 || y2 < 0)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.argument.outofrange.world") + ": y < 0");
        }

        if (y1 > 255 || y2 > 255)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.argument.outofrange.world") + ": y > 255");
        }

        if (x1 < -30000000 || x2 < -30000000 || z1 < -30000000 || z2 < -30000000)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.argument.outofrange.world") + ": x or z < -30000000");
        }

        if (x1 > 30000000 || x2 > 30000000 || z1 > 30000000 || z2 > 30000000)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.argument.outofrange.world") + ": x or z > 30000000");
        }

        if (Math.abs(x1 - x2) > 512 || Math.abs(z1 - z2) > 512)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.argument.outofrange.toolarge"));
        }

        return true;
    }

    public HashMap<String, BlockInfo> calculateBlockStats(EntityPlayer player, List<String> ranges) throws CommandException
    {
        if (player == null)
        {
            return null;
        }

        int x = (int)player.posX, y = (int)player.posY, z = (int)player.posZ;
        int range_x = 0, range_y = 0, range_z = 0;

        try
        {
            range_x = Math.abs(Integer.parseInt(ranges.get(0)));
            range_y = Math.abs(Integer.parseInt(ranges.get(1)));
            range_z = Math.abs(Integer.parseInt(ranges.get(2)));
        }
        catch (NumberFormatException e)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.invalid.argument.number"));
        }

        // We don't allow ranges over 256 blocks from the player
        if (range_x > 256 || range_z > 256)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.argument.outofrange") + ": x or z > 256");
        }

        int y_min = (y - range_y) >=   0 ? y - range_y :   0;
        int y_max = (y + range_y) <= 255 ? y + range_y : 255;

        this.areCoordinatesValid(x - range_x, y_min, z - range_z, x + range_x, y_max, z + range_z);

        if (this.checkChunksAreLoaded(player.dimension, x - range_x, z - range_z, x + range_x, z + range_z) == false)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.subcommand.blockstats.chunksnotloaded"));
        }

        this.calculateBlockStats(player.dimension, x - range_x, y_min, z - range_z, x + range_x, y_max, z + range_z);

        return this.blockStats;
    }

    public HashMap<String, BlockInfo> calculateBlockStats(List<String> params) throws CommandException
    {
        int dim = 0, x1 = 0, y1 = 0, z1 = 0, x2 = 0, y2 = 0, z2 = 0;

        try
        {
            dim = Integer.parseInt(params.get(0));
            x1 = Integer.parseInt(params.get(1));
            y1 = Integer.parseInt(params.get(2));
            z1 = Integer.parseInt(params.get(3));
            x2 = Integer.parseInt(params.get(4));
            y2 = Integer.parseInt(params.get(5));
            z2 = Integer.parseInt(params.get(6));
        }
        catch (NumberFormatException e)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.invalid.argument.number"));
        }

        if (this.checkChunksAreLoaded(dim, x1, z1, x2, z2) == false)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.subcommand.blockstats.chunksnotloaded"));
        }

        int tmp;
        if (x1 > x2) { tmp = x1; x1 = x2; x2 = tmp; }
        if (y1 > y2) { tmp = y1; y1 = y2; y2 = tmp; }
        if (z1 > z2) { tmp = z1; z1 = z2; z2 = tmp; }

        int y_min = y1 >=   0 ? y1 :   0;
        int y_max = y2 <= 255 ? y2 : 255;

        this.areCoordinatesValid(x1, y_min, z1, x2, y_max, z2);

        if (this.checkChunksAreLoaded(dim, x1, z1, x2, z2) == false)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.subcommand.blockstats.chunksnotloaded"));
        }

        this.calculateBlockStats(dim, x1, y_min, z1, x2, y_max, z2);

        return this.blockStats;
    }

    private void calculateBlockStats(int dim, int x1, int y1, int z1, int x2, int y2, int z2) throws CommandException
    {
        //System.out.printf("dim: %d x1: %d, y1: %d, z1: %d x2: %d y2: %d z2: %d\n", dim, x1, y1, z1, x2, y2, z2);

        WorldServer worldServer = MinecraftServer.getServer().worldServerForDimension(dim);
        if (worldServer == null)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.subcommand.blockstats.invalid.dimension") + ": " + dim);
        }

        this.blockStats = new HashMap<String, BlockInfo>();
        this.longestName = 0;
        this.longestDisplayName = 0;
        int[] counts = new int[65536];
        int[] countsTE = new int[65536];
        IBlockState iBlockState;
        Block block;
        int count = 0, nulls = 0, index = 0;

        // TODO profile this too:
        // 23:13:48 < diesieben07> there is BlockPos.getAllInBoxMutable
        // 23:14:06 < diesieben07> which returns an Iterable for all BlockPos' in a box, but re-uses the same instance

        // Calculate the number of each block type identified by: "id << 4 | meta"
        for (int y = y1; y <= y2; ++y)
        {
            for (int x = x1; x <= x2; ++x)
            {
                for (int z = z1; z <= z2; ++z)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    iBlockState = worldServer.getBlockState(pos);
                    block = iBlockState.getBlock();
                    if (block != null)
                    {
                        index = Block.getIdFromBlock(block) << 4 | (block.getMetaFromState(iBlockState) & 0xF);
                        count++;
                        counts[index]++;

                        // Count the TileEntities for each block type
                        if (worldServer.getTileEntity(pos) != null)
                        {
                            countsTE[index]++;
                        }
                    }
                    else
                    {
                        nulls++;
                    }
                }
            }
        }

        TellMe.logger.info("Counted " + count + " blocks; " + nulls + " blocks were null.");

        String name;
        String dname;
        int id = 0, meta = 0;

        for (int i = 0; i < 65536; ++i)
        {
            if (counts[i] > 0)
            {
                try
                {
                    id = i >> 4;
                    meta = i & 0xF;
                    block = Block.getBlockById(id);

                    // We don't want to use getItemDropped(), that would turn Stone into Cobblestone etc.
                    //ItemStack stack = new ItemStack(block.getItemDropped(meta, worldServer.rand, 0), 1, block.damageDropped(meta));

                    ItemStack stack = new ItemStack(block, 1, block.damageDropped(block.getStateFromMeta(meta)));
                    name = Block.blockRegistry.getNameForObject(block).toString();

                    if (stack != null && stack.getItem() != null)
                    {
                        dname = stack.getDisplayName();
                    }
                    // Blocks that are not obtainable/don't have an ItemBlock
                    else
                    {
                        dname = name;
                    }

                    this.blockStats.put(name + ":" + meta, new BlockInfo(name, dname, id, meta, counts[i], countsTE[i]));

                    if (name.length() > this.longestName)
                    {
                        this.longestName = name.length();
                    }

                    if (dname.length() > this.longestDisplayName)
                    {
                        this.longestDisplayName = dname.length();
                    }
                }
                catch (Exception e)
                {
                    TellMe.logger.error("Caught an exception while getting block names");
                    TellMe.logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void queryAll()
    {
        this.query(null);
    }

    public void query(List<String> filters)
    {
        int nameLen = this.longestName;
        int dNameLen = this.longestDisplayName;

        //ArrayList<String> keys = new ArrayList<String>();
        //keys.addAll(this.blockStats.keySet());
        //Collections.sort(keys);

        ArrayList<BlockInfo> values = new ArrayList<BlockInfo>();
        values.addAll(this.blockStats.values());
        Collections.sort(values);

        // Get the longest name lengths from the filtered list for formatting the output nicely
        if (filters != null)
        {
            nameLen = 0;
            dNameLen = 0;

            for (BlockInfo blockInfo : values)
            {
                if (this.filterFound(filters, blockInfo) == true)
                {
                    if (blockInfo.name.length() > nameLen)
                    {
                        nameLen = blockInfo.name.length();
                    }

                    if (blockInfo.displayName.length() > dNameLen)
                    {
                        dNameLen = blockInfo.displayName.length();
                    }
                }
            }

            if (nameLen < 10)
            {
                nameLen = 10;
            }

            if (dNameLen < 12)
            {
                dNameLen = 12;
            }
        }

        StringBuilder separator = new StringBuilder(256);
        int len = nameLen + dNameLen + 35;
        for (int i = 0; i < len; ++i) { separator.append("-"); }

        this.blockStatLines = new ArrayList<String>();
        this.blockStatLines.add(separator.toString());
        this.blockStatLines.add("*** NOTE *** The Block ID is for very specific debugging or fixing purposes only!!!");
        this.blockStatLines.add("It WILL be different on every world since Minecraft 1.7, since they are dynamically allocated by the game!!!");
        this.blockStatLines.add(separator.toString());

        String fmt = String.format("%%-%ds | %%-%ds | %%8d | %%4d:%%-2d | %%8d", nameLen, dNameLen);
        String fmt2 = String.format("%%-%ds | %%-%ds", nameLen, dNameLen);

        this.blockStatLines.add(String.format(fmt2 + " | %8s | %7s | %8s", "Block name", "Display name", "Count", "ID:meta", "Count TE"));
        this.blockStatLines.add(separator.toString());

        for (BlockInfo blockInfo : values)
        {
            if (filters == null || this.filterFound(filters, blockInfo) == true)
            {
                this.blockStatLines.add(String.format(fmt, blockInfo.name, blockInfo.displayName, blockInfo.count, blockInfo.id, blockInfo.meta, blockInfo.countTE));
            }
        }
    }

    private boolean filterMatches(String filter, BlockInfo info)
    {
        int first = filter.indexOf(":");

        // At least one ':' found
        if (first != -1)
        {
            int last = filter.lastIndexOf(":");

            // At least two ':' characters found; assume the first separates the modid and block name, and the second separates the block name and meta
            if (last != first && last < (filter.length() - 1))
            {
                try
                {
                    int meta = Integer.parseInt(filter.substring(last + 1, filter.length()));
                    if (filter.substring(0, last).equals(info.name) && meta == info.meta)
                    {
                        return true;
                    }
                }
                catch (NumberFormatException e)
                {
                    TellMe.logger.error("Caught an exception while parsing block meta value from user input");
                    e.printStackTrace();
                }
            }
            // else: Just one ':' character found. We should have matched before calling this method, if it was in the modid:blockname format.
            // And if it is not, then we don't support it (blockname:meta without modid) anyway.
        }
        // No ':' characters found, assume simple vanilla block name
        else
        {
            if (info.name.equals("minecraft:" + filter))
            {
                return true;
            }
        }

        return false;
    }

    private boolean filterFound(List<String> filters, BlockInfo info)
    {
        // FIXME It would probably be more efficient to loop the filter list since it's probably shorter,
        // and pick the requested things from the block info list. Probably won't make much of a difference though in practice.

        // Simple case, the input name is a fully qualified block name
        if (filters.contains(info.name))
        {
            return true;
        }

        // Try to parse the filter strings and handle possible meta restrictions etc.
        for (String filter : filters)
        {
            if (this.filterMatches(filter, info) == true)
            {
                return true;
            }
        }

        return false;
    }

    public void printBlockStatsToLogger()
    {
        for (String line : this.blockStatLines)
        {
            TellMe.logger.info(line);
        }
    }

    public ArrayList<String> getBlockStatsLines()
    {
        return this.blockStatLines;
    }
}
