package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;

public class BlockStats
{
    private HashMap<String, BlockInfo> blockStats;
    private List<String> blockStatLines;
    private BlockPos pos1;
    private BlockPos pos2;
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

    private void setAndFixPositions(BlockPos pos1, BlockPos pos2)
    {
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int yMax = Math.max(pos1.getY(), pos2.getY());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());

        yMin = MathHelper.clamp(yMin, 0, 255);
        yMax = MathHelper.clamp(yMax, 0, 255);

        this.pos1 = new BlockPos(xMin, yMin, zMin);
        this.pos2 = new BlockPos(xMax, yMax, zMax);
    }

    private boolean checkChunksAreLoaded(World world)
    {
        return world.isAreaLoaded(this.pos1, this.pos2, true);
    }

    private boolean areCoordinatesValid() throws CommandException
    {
        if (this.pos1.getY() < 0 || this.pos2.getY() < 0)
        {
            throw new WrongUsageException("tellme.command.error.argument.outofrange.world", "y < 0");
        }

        if (this.pos1.getY() > 255 || this.pos2.getY() > 255)
        {
            throw new WrongUsageException("tellme.command.error.argument.outofrange.world", "y > 255");
        }

        if (this.pos1.getX() < -30000000 || this.pos2.getX() < -30000000 || this.pos1.getZ() < -30000000 || this.pos2.getZ() < -30000000)
        {
            throw new WrongUsageException("tellme.command.error.argument.outofrange.world", "x or z < -30M");
        }

        if (this.pos1.getX() > 30000000 || this.pos2.getX() > 30000000 || this.pos1.getZ() > 30000000 || this.pos2.getZ() > 30000000)
        {
            throw new WrongUsageException("tellme.command.error.argument.outofrange.world", "x or z > 30M");
        }

        if (Math.abs(this.pos1.getX() - this.pos2.getX()) > 512 || Math.abs(this.pos1.getZ() - this.pos2.getZ()) > 512)
        {
            throw new WrongUsageException("tellme.command.error.argument.outofrange.toolarge");
        }

        return true;
    }

    public void calculateBlockStats(World world, BlockPos playerPos, int rangeX, int rangeY, int rangeZ) throws CommandException
    {
        BlockPos pos1 = playerPos.add(-rangeX, -rangeY, -rangeZ);
        BlockPos pos2 = playerPos.add( rangeX,  rangeY,  rangeZ);

        this.calculateBlockStats(world, pos1, pos2);
    }

    public void calculateBlockStats(World world, BlockPos pos1, BlockPos pos2) throws CommandException
    {
        this.setAndFixPositions(pos1, pos2);
        this.areCoordinatesValid();
        this.calculateBlockStats(world);
    }

    private void calculateBlockStats(World world) throws CommandException
    {
        //System.out.printf("dim: %d x1: %d, y1: %d, z1: %d x2: %d y2: %d z2: %d\n", dim, x1, y1, z1, x2, y2, z2);

        if (this.checkChunksAreLoaded(world) == false)
        {
            throw new WrongUsageException("tellme.subcommand.blockstats.error.chunksnotloaded");
        }

        int x1 = this.pos1.getX();
        int y1 = this.pos1.getY();
        int z1 = this.pos1.getZ();
        int x2 = this.pos2.getX();
        int y2 = this.pos2.getY();
        int z2 = this.pos2.getZ();

        this.blockStats = new HashMap<String, BlockInfo>();
        this.longestName = 0;
        this.longestDisplayName = 0;
        int[] counts = new int[65536];
        int[] countsTE = new int[65536];
        IBlockState iBlockState;
        Block block;
        int count = 0, index = 0;

        // TODO profile this too:
        // 23:13:48 < diesieben07> there is BlockPos.getAllInBoxMutable
        // 23:14:06 < diesieben07> which returns an Iterable for all BlockPos' in a box, but re-uses the same instance

        long timeBefore = System.currentTimeMillis();
        MutableBlockPos pos = new MutableBlockPos(0, 0, 0);

        // Calculate the number of each block type identified by: "id << 4 | meta"
        for (int y = y1; y <= y2; ++y)
        {
            for (int x = x1; x <= x2; ++x)
            {
                for (int z = z1; z <= z2; ++z)
                {
                    pos.setPos(x, y, z);
                    iBlockState = world.getBlockState(pos);
                    block = iBlockState.getBlock();

                    index = Block.getIdFromBlock(block) << 4 | (block.getMetaFromState(iBlockState) & 0xF);
                    count++;
                    counts[index]++;

                    // Count the TileEntities for each block type
                    if (world.getTileEntity(pos) != null)
                    {
                        countsTE[index]++;
                    }
                }
            }
        }

        long timeAfter = System.currentTimeMillis();
        TellMe.logger.info(String.format(Locale.US, "Counted %d blocks in %.3f seconds.", count, (timeAfter - timeBefore) / 1000f));

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

                    @SuppressWarnings("deprecation")
                    ItemStack stack = new ItemStack(block, 1, block.damageDropped(block.getStateFromMeta(meta)));
                    name = Block.REGISTRY.getNameForObject(block).toString();

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
                if (this.filterFound(filters, blockInfo))
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
            if (filters == null || this.filterFound(filters, blockInfo))
            {
                this.blockStatLines.add(String.format(fmt, blockInfo.name, blockInfo.displayName, blockInfo.count, blockInfo.id, blockInfo.meta, blockInfo.countTE));
            }
        }

        this.blockStatLines.add(separator.toString());
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
            if (this.filterMatches(filter, info))
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

    public List<String> getBlockStatsLines()
    {
        return this.blockStatLines;
    }
}
