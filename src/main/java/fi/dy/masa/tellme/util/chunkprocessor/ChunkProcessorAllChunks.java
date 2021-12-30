package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.Collection;
import java.util.List;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import fi.dy.masa.tellme.util.WorldUtils;

public abstract class ChunkProcessorAllChunks
{
    private boolean areCoordinatesValid(BlockPos pos1, BlockPos pos2) throws CommandSyntaxException
    {
        if (pos1.getY() < 0 || pos2.getY() < 0)
        {
            throw (new SimpleCommandExceptionType(new TranslatableComponent("Argument(s) out of range: y < 0"))).create();
        }

        if (pos1.getY() > 255 || pos2.getY() > 255)
        {
            throw (new SimpleCommandExceptionType(new TranslatableComponent("Argument(s) out of range: y > 255"))).create();
        }

        if (pos1.getX() < -30000000 || pos2.getX() < -30000000 || pos1.getZ() < -30000000 || pos2.getZ() < -30000000)
        {
            throw (new SimpleCommandExceptionType(new TranslatableComponent("Argument(s) out of range (world limits): x or z < -30M"))).create();
        }

        if (pos1.getX() > 30000000 || pos2.getX() > 30000000 || pos1.getZ() > 30000000 || pos2.getZ() > 30000000)
        {
            throw (new SimpleCommandExceptionType(new TranslatableComponent("Argument(s) out of range (world limits): x or z > 30M"))).create();
        }

        return true;
    }

    public void processChunks(Level world, BlockPos posMin, BlockPos posMax) throws CommandSyntaxException
    {
        if (this.areCoordinatesValid(posMin, posMax) == false)
        {
            throw (new SimpleCommandExceptionType(new TranslatableComponent("Invalid coordinate(s) in the range, aborting"))).create();
        }

        ChunkPos chunkPosMin = new ChunkPos(posMin.getX() >> 4, posMin.getZ() >> 4);
        ChunkPos chunkPosMax = new ChunkPos(posMax.getX() >> 4, posMax.getZ() >> 4);

        List<LevelChunk> chunks = WorldUtils.loadAndGetChunks(world, chunkPosMin, chunkPosMax);

        this.processChunks(chunks, posMin, posMax);
    }

    public void processChunks(Collection<LevelChunk> chunks)
    {
        this.processChunks(chunks, new BlockPos(-30000000, 0, -30000000), new BlockPos(30000000, 255, 30000000));
    }

    public abstract void processChunks(Collection<LevelChunk> chunks, BlockPos posMin, BlockPos posMax);
}
