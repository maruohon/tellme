package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import fi.dy.masa.tellme.TellMe;

public class BlockStats
{
    private HashMap<String, BlockInfo> blockStats;
    private ArrayList<String> blockStatLines;
    private int longestName = 0;

    public class BlockInfo implements Comparable<BlockInfo>
    {
        public String name;
        public int id;
        public int meta;
        public int count;
        public int countTE;

        public BlockInfo(String name, int id, int meta, int count, int countTE)
        {
            this.name = name;
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
        return true;
    }

    private boolean areCoordinatesValid(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        if (y1 < 0 || y2 < 0)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.argument.outofrange.world") + ": y < 0");
        }

        if (y1 >= 256 || y2 >= 256)
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

        return true;
    }

    public HashMap<String, BlockInfo> calculateBlockStats(EntityPlayer player, List<String> ranges)
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
        if (range_x > 256 || range_y > 256 || range_z > 256)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.command.argument.outofrange") + ": > 256");
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

    public HashMap<String, BlockInfo> calculateBlockStats(List<String> params)
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

    private void calculateBlockStats(int dim, int x1, int y1, int z1, int x2, int y2, int z2)
    {
        //System.out.printf("dim: %d x1: %d, y1: %d, z1: %d x2: %d y2: %d z2: %d\n", dim, x1, y1, z1, x2, y2, z2);

        WorldServer worldServer = MinecraftServer.getServer().worldServerForDimension(dim);
        if (worldServer == null)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.subcommand.blockstats.invalid.dimension") + ": " + dim);
        }

        this.blockStats = new HashMap<String, BlockInfo>();
        this.longestName = 0;
        int[] counts = new int[65536];
        int[] countsTE = new int[65536];
        Block block;
        int count = 0, nulls = 0, index = 0;

        // Calculate the number of each block type identified by: "id << 4 | meta"
        for (int y = y1; y <= y2; ++y)
        {
            for (int x = x1; x <= x2; ++x)
            {
                for (int z = z1; z <= z2; ++z)
                {
                    block = worldServer.getBlock(x, y, z);
                    if (block != null)
                    {
                        index = Block.getIdFromBlock(block) << 4 | (worldServer.getBlockMetadata(x, y, z) & 0xF);
                        count++;
                        counts[index]++;

                        // Count the TileEntities for each block type
                        if (worldServer.getTileEntity(x, y, z) != null)
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
        for (int i = 0; i < 65536; ++i)
        {
            if (counts[i] > 0)
            {
                try
                {
                    block = Block.getBlockById(i >> 4);

                    // We don't want to use getItemDropped(), that would turn Stone into Cobblestone etc.
                    //ItemStack stack = new ItemStack(block.getItemDropped((i & 0xF), worldServer.rand, 0), 1, block.damageDropped(i & 0xF));

                    ItemStack stack = new ItemStack(Block.getBlockById(i >> 4), 1, i & 0xF);

                    //name = Block.blockRegistry.getNameForObject(Block.getBlockById(i >> 4)) + ":" + (i & 0xF);
                    //name = GameData.getItemRegistry().getNameForObject(stack.getItem()) + ":" + (i & 0xF);

                    if (stack != null && stack.getItem() != null)
                    {
                        name = stack.getDisplayName();
                    }
                    // Mostly Air?
                    else
                    {
                        name = Block.blockRegistry.getNameForObject(Block.getBlockById(i >> 4));
                    }

                    this.blockStats.put(name + ":" + (i & 0xF), new BlockInfo(name, (i >> 4), (i & 0xF), counts[i], countsTE[i]));
                    if (name.length() > this.longestName)
                    {
                        this.longestName = name.length();
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
        String fmt = String.format("%%-%ds | %%8d | %%4d:%%-2d | %%8d", this.longestName + 1);
        //ArrayList<String> keys = new ArrayList<String>();
        //keys.addAll(this.blockStats.keySet());
        //Collections.sort(keys);
        ArrayList<BlockInfo> values = new ArrayList<BlockInfo>();
        values.addAll(this.blockStats.values());
        Collections.sort(values);
        this.blockStatLines = new ArrayList<String>();
        String fmt2 = String.format("%%-%ds", this.longestName + 1);
        this.blockStatLines.add("*** NOTE *** The Block ID is for very specific debugging or fixing purposes only!!!");
        this.blockStatLines.add("It WILL be different on every world since Minecraft 1.7, since they are dynamically allocated by the game!!!");
        this.blockStatLines.add("------------------------------------------------------------------------------------------------------------");
        this.blockStatLines.add(String.format(fmt2 + " | %8s | %7s | %8s", "Block name", "Count", "ID:meta", "Count TE"));
        this.blockStatLines.add(this.blockStatLines.get(2).substring(0, this.longestName + 33));

        for (BlockInfo blockInfo : values)
        {
            // FIXME handle the formatting of the name in filters
            if (filters == null || filters.contains(blockInfo.name))
            {
                //BlockInfo blockInfo = this.blockStats.get(name);
                this.blockStatLines.add(String.format(fmt, blockInfo.name, blockInfo.count, blockInfo.id, blockInfo.meta, blockInfo.countTE));
            }
        }
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
