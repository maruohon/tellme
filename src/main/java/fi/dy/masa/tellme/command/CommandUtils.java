package fi.dy.masa.tellme.command;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandUtils
{
    public static final FilenameFilter FILTER_FILES = (pathName, fileName) -> { return (new File(pathName, fileName)).isFile(); };
    public static final SimpleCommandExceptionType NO_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(new TextComponent("This command must either be executed by an entity, or the dimension must be specified"));
    public static final DynamicCommandExceptionType DIMENSION_NOT_LOADED_EXCEPTION = new DynamicCommandExceptionType((type) -> new TextComponent("The dimension \"" + type + "\" was not loaded"));
    public static final SimpleCommandExceptionType NOT_A_PLAYER_EXCEPTION = new SimpleCommandExceptionType(new TextComponent("This command must be executed by a player"));
    public static final DynamicCommandExceptionType INVALID_OUTPUT_TYPE_EXCEPTION = new DynamicCommandExceptionType((type) -> new TextComponent("Invalid output type: " + type));

    public static BlockPos getMinCorner(Vec2 pos1, Vec2 pos2, Level world)
    {
        return new BlockPos(Math.min(Mth.floor(pos1.x), Mth.floor(pos2.x)),
                            world.getMinBuildHeight(),
                            Math.min(Mth.floor(pos1.y), Mth.floor(pos2.y)));
    }

    public static BlockPos getMaxCorner(Vec2 pos1, Vec2 pos2, Level world)
    {
        return new BlockPos(Math.max(Mth.floor(pos1.x), Mth.floor(pos2.x)),
                            world.getMaxBuildHeight() - 1,
                            Math.max(Mth.floor(pos1.y), Mth.floor(pos2.y)));
    }

    public static BlockPos getMinCorner(Vec3 pos1, Vec3 pos2)
    {
        return new BlockPos(Math.min(Mth.floor(pos1.x), Mth.floor(pos2.x)),
                            Math.min(Mth.floor(pos1.y), Mth.floor(pos2.y)),
                            Math.min(Mth.floor(pos1.z), Mth.floor(pos2.z)));
    }

    public static BlockPos getMaxCorner(Vec3 pos1, Vec3 pos2)
    {
        return new BlockPos(Math.max(Mth.floor(pos1.x), Mth.floor(pos2.x)),
                            Math.max(Mth.floor(pos1.y), Mth.floor(pos2.y)),
                            Math.max(Mth.floor(pos1.z), Mth.floor(pos2.z)));
    }

    /**
     * Gets the given Vector2f as a ChunkPos, converting the coordinates from block to chunk coordinates
     * @param vec
     * @return
     */
    public static ChunkPos getAsChunkPos(Vec2 vec)
    {
        return new ChunkPos(Mth.floor(vec.x) >> 4, Mth.floor(vec.y) >> 4);
    }

    /**
     * Takes in block coordinates, outputs chunk coordinates
     * @param pos1
     * @param pos2
     * @return
     */
    public static ChunkPos getMinCornerChunkPos(Vec2 pos1, Vec2 pos2)
    {
        return new ChunkPos(Math.min(Mth.floor(pos1.x) >> 4, Mth.floor(pos2.x) >> 4),
                            Math.min(Mth.floor(pos1.y) >> 4, Mth.floor(pos2.y) >> 4));
    }

    /**
     * Takes in block coordinates, outputs chunk coordinates
     * @param pos1
     * @param pos2
     * @return
     */
    public static ChunkPos getMaxCornerChunkPos(Vec2 pos1, Vec2 pos2)
    {
        return new ChunkPos(Math.max(Mth.floor(pos1.x) >> 4, Mth.floor(pos2.x) >> 4),
                            Math.max(Mth.floor(pos1.y) >> 4, Mth.floor(pos2.y) >> 4));
    }

    /**
     * Takes in block coordinates, outputs chunk coordinates
     */
    public static ChunkPos getMinCornerChunkPos(Vec3 pos1, Vec3 pos2)
    {
        return new ChunkPos(Math.min(Mth.floor(pos1.x) >> 4, Mth.floor(pos2.x) >> 4),
                            Math.min(Mth.floor(pos1.z) >> 4, Mth.floor(pos2.z) >> 4));
    }

    /**
     * Takes in block coordinates, outputs chunk coordinates
     */
    public static ChunkPos getMaxCornerChunkPos(Vec3 pos1, Vec3 pos2)
    {
        return new ChunkPos(Math.max(Mth.floor(pos1.x) >> 4, Mth.floor(pos2.x) >> 4),
                            Math.max(Mth.floor(pos1.z) >> 4, Mth.floor(pos2.z) >> 4));
    }

    public static Vec3 getMinCornerVec3d(Vec3 pos1, Vec3 pos2)
    {
        return new Vec3(Math.min(pos1.x, pos2.x),
                            Math.min(pos1.y, pos2.y),
                            Math.min(pos1.z, pos2.z));
    }

    public static Vec3 getMaxCornerVec3d(Vec3 pos1, Vec3 pos2)
    {
        return new Vec3(Math.max(pos1.x, pos2.x),
                            Math.max(pos1.y, pos2.y),
                            Math.max(pos1.z, pos2.z));
    }

    public static CompletableFuture<Suggestions> suggestIterable(Iterable<String> iterable, SuggestionsBuilder builder)
    {
        String arg = builder.getRemaining().toLowerCase(Locale.ROOT);

        for (String str : iterable)
        {
            if (str.startsWith(arg))
            {
                builder.suggest(str);
            }
        }

        return builder.buildFuture();
    }

    public static List<String> getFileNames(File dir, FilenameFilter filter)
    {
        if (dir.isDirectory())
        {
            String[] names = dir.list(filter);
            List<String> files = Arrays.asList(names);
            files.sort(String::compareTo);
            return files;
        }

        return Collections.emptyList();
    }

    public static void throwException(String message) throws CommandSyntaxException
    {
        throw (new SimpleCommandExceptionType(new TranslatableComponent(message))).create();
    }

    public static void sendMessage(CommandSourceStack source, String message)
    {
        source.sendSuccess(new TextComponent(message), true);
    }

    public static Vec2 getVec2fFromSource(CommandSourceStack source)
    {
        Entity entity = source.getEntity();
        return entity != null ? new Vec2((float) entity.getX(), (float) entity.getZ()) : Vec2.ZERO;
    }

    public static Vec2 getVec2fFromArg(CommandContext<CommandSourceStack> ctx, String argName) throws CommandSyntaxException
    {
        return Vec2Argument.getVec2(ctx, argName);
    }

    public static Vec3 getVec3dFromSource(CommandSourceStack source)
    {
        Entity entity = source.getEntity();
        return entity != null ? entity.position() : Vec3.ZERO;
    }

    public static BlockPos getBlockPosFromSource(CommandSourceStack source)
    {
        Entity entity = source.getEntity();
        return entity != null ? new BlockPos(entity.position()) : BlockPos.ZERO;
    }

    public static Vec3 getVec3dFromArg(CommandContext<CommandSourceStack> ctx, String argName) throws CommandSyntaxException
    {
        return Vec3Argument.getVec3(ctx, argName);
    }

    public static Level getWorldFromCommandSource(CommandSourceStack source) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity == null)
        {
            throw NO_DIMENSION_EXCEPTION.create();
        }

        return entity.getCommandSenderWorld();
    }

    public interface IWorldRetriever
    {
        Level getWorldFromSource(CommandSourceStack source) throws CommandSyntaxException;
    }

    public enum OutputType
    {
        CHAT        ("to-chat"),
        CONSOLE     ("to-console"),
        FILE        ("to-file");

        private final String arg;

        OutputType(String arg)
        {
            this.arg = arg;
        }

        public String getArgument()
        {
            return this.arg;
        }

        @Nullable
        public static OutputType fromArg(String arg)
        {
            for (OutputType type : OutputType.values())
            {
                if (type.arg.equals(arg))
                {
                    return type;
                }
            }

            return null;
        }
    }

    public enum AreaType
    {
        AREA            ("area"),
        BOX             ("box"),
        CHUNK_RADIUS    ("chunk-radius"),
        LOADED          ("loaded-chunks"),
        RANGE           ("range"),
        SAMPLED         ("sampled");

        private final String arg;

        AreaType(String arg)
        {
            this.arg = arg;
        }

        public String getArgument()
        {
            return this.arg;
        }

        @Nullable
        public static AreaType fromArg(String arg)
        {
            for (AreaType type : AreaType.values())
            {
                if (type.arg.equals(arg))
                {
                    return type;
                }
            }

            return null;
        }
    }

    public enum BlockStateGrouping
    {
        BY_BLOCK("by-block"),
        BY_STATE("by-state");

        private final String arg;

        BlockStateGrouping(String arg)
        {
            this.arg = arg;
        }

        public String getArgument()
        {
            return this.arg;
        }

        @Nullable
        public static BlockStateGrouping fromArg(String arg)
        {
            for (BlockStateGrouping type : BlockStateGrouping.values())
            {
                if (type.arg.equals(arg))
                {
                    return type;
                }
            }

            return null;
        }
    }
}
