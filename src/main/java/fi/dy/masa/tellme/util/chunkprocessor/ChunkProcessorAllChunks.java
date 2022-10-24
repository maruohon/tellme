package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import fi.dy.masa.tellme.util.WorldUtils;

public abstract class ChunkProcessorAllChunks
{
    private boolean areCoordinatesValid(BlockPos pos1, BlockPos pos2) throws CommandException
    {
        if (pos1.getY() < 0 || pos2.getY() < 0)
        {
            throw new WrongUsageException("Argument(s) out of range: y < 0");
        }

        if (pos1.getY() > 255 || pos2.getY() > 255)
        {
            throw new WrongUsageException("Argument(s) out of range: y > 255");
        }

        if (pos1.getX() < -30000000 || pos2.getX() < -30000000 || pos1.getZ() < -30000000 || pos2.getZ() < -30000000)
        {
            throw new WrongUsageException("Argument(s) out of range (world limits): x or z < -30M");
        }

        if (pos1.getX() > 30000000 || pos2.getX() > 30000000 || pos1.getZ() > 30000000 || pos2.getZ() > 30000000)
        {
            throw new WrongUsageException("Argument(s) out of range (world limits): x or z > 30M");
        }

        return true;
    }

    private Pair<BlockPos, BlockPos> getCorners(BlockPos pos1, BlockPos pos2)
    {
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int yMax = Math.max(pos1.getY(), pos2.getY());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());

        yMin = MathHelper.clamp(yMin, 0, 255);
        yMax = MathHelper.clamp(yMax, 0, 255);

        return Pair.of(new BlockPos(xMin, yMin, zMin), new BlockPos(xMax, yMax, zMax));
    }

    public void processChunks(World world, BlockPos playerPos, int rangeX, int rangeY, int rangeZ) throws CommandException
    {
        BlockPos pos1 = playerPos.add(-rangeX, -rangeY, -rangeZ);
        BlockPos pos2 = playerPos.add( rangeX,  rangeY,  rangeZ);

        this.processChunks(world, pos1, pos2);
    }

    public void processChunks(World world, BlockPos pos1, BlockPos pos2) throws CommandException
    {
        Pair<BlockPos, BlockPos> pair = this.getCorners(pos1, pos2);
        BlockPos posMin = pair.getLeft();
        BlockPos posMax = pair.getRight();

        if (this.areCoordinatesValid(posMin, posMax) == false)
        {
            throw new WrongUsageException("Invalid coordinate(s) in the range, aborting");
        }

        ChunkPos chunkPosMin = new ChunkPos(posMin.getX() >> 4, posMin.getZ() >> 4);
        ChunkPos chunkPosMax = new ChunkPos(posMax.getX() >> 4, posMax.getZ() >> 4);

        List<Chunk> chunks = WorldUtils.loadAndGetChunks(world, chunkPosMin, chunkPosMax);

        this.processChunks(chunks, posMin, posMax);
    }

    public void processChunks(Collection<Chunk> chunks)
    {
        this.processChunks(chunks, new BlockPos(-30000000, 0, -30000000), new BlockPos(30000000, 255, 30000000));
    }

    public abstract void processChunks(Collection<Chunk> chunks, BlockPos posMin, BlockPos posMax);
}
