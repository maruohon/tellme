package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Collection;
import java.util.List;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import fi.dy.masa.tellme.util.WorldUtils;

public abstract class ChunkProcessorAllChunks
{
    private boolean areCoordinatesValid(BlockPos pos1, BlockPos pos2, World world) throws CommandSyntaxException
    {
        int minY = world.getBottomY();
        int maxY = world.getTopY() - 1;

        if (pos1.getY() < minY || pos2.getY() < minY)
        {
            throw (new SimpleCommandExceptionType(Text.literal("Argument(s) out of range: y < " + minY))).create();
        }

        if (pos1.getY() > maxY || pos2.getY() > maxY)
        {
            throw (new SimpleCommandExceptionType(Text.literal("Argument(s) out of range: y > " + maxY))).create();
        }

        if (pos1.getX() < -30000000 || pos2.getX() < -30000000 || pos1.getZ() < -30000000 || pos2.getZ() < -30000000)
        {
            throw (new SimpleCommandExceptionType(Text.literal("Argument(s) out of range (world limits): x or z < -30M"))).create();
        }

        if (pos1.getX() > 30000000 || pos2.getX() > 30000000 || pos1.getZ() > 30000000 || pos2.getZ() > 30000000)
        {
            throw (new SimpleCommandExceptionType(Text.literal("Argument(s) out of range (world limits): x or z > 30M"))).create();
        }

        return true;
    }

    public void processChunks(World world, BlockPos posMin, BlockPos posMax) throws CommandSyntaxException
    {
        if (this.areCoordinatesValid(posMin, posMax, world) == false)
        {
            throw (new SimpleCommandExceptionType(Text.literal("Invalid coordinate(s) in the range, aborting"))).create();
        }

        ChunkPos chunkPosMin = new ChunkPos(posMin.getX() >> 4, posMin.getZ() >> 4);
        ChunkPos chunkPosMax = new ChunkPos(posMax.getX() >> 4, posMax.getZ() >> 4);

        List<WorldChunk> chunks = WorldUtils.loadAndGetChunks(world, chunkPosMin, chunkPosMax);

        this.processChunks(chunks, posMin, posMax);
    }

    public void processChunks(Collection<WorldChunk> chunks, World world)
    {
        int minY = world.getBottomY();
        int maxY = world.getTopY() - 1;
        this.processChunks(chunks, new BlockPos(-30000000, minY, -30000000), new BlockPos(30000000, maxY, 30000000));
    }

    public abstract void processChunks(Collection<WorldChunk> chunks, BlockPos posMin, BlockPos posMax);
}
