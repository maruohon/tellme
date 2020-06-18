package fi.dy.masa.tellme.command;

import java.util.List;
import java.util.stream.Collectors;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.command.arguments.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.chunkprocessor.Locate;
import fi.dy.masa.tellme.util.chunkprocessor.Locate.LocateType;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandLocate
{
    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("locate").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createNodes(LocateType.BLOCK));
        subCommandRootNode.addChild(createNodes(LocateType.ENTITY));
        subCommandRootNode.addChild(createNodes(LocateType.TILE_ENTITY));

        return subCommandRootNode;
    }

    private static int printHelp(ServerCommandSource source)
    {
        CommandUtils.sendMessage(source, "Locates Blocks or TileEntities or Entities in the current dimension");
        CommandUtils.sendMessage(source, "Usage: /tellme locate <block | entity | te> <to-chat | to-console | to-file> <ascii | csv> all-loaded-chunks <name> [name name ...]");
        CommandUtils.sendMessage(source, "Usage: /tellme locate <block | entity | te> <to-chat | to-console | to-file> <ascii | csv> box <x1> <y1> <z1> <x2> <y2> <z2> <name> [name name ...]");
        CommandUtils.sendMessage(source, "Usage: /tellme locate <block | entity | te> <to-chat | to-console | to-file> <ascii | csv> chunk-radius <radius> <name> [name name ...]");

        return 1;
    }

    private static LiteralCommandNode<ServerCommandSource> createNodes(LocateType type)
    {
        LiteralCommandNode<ServerCommandSource> argTarget = CommandManager.literal(type.getArgument()).build();

        ArgumentCommandNode<ServerCommandSource, CommandUtils.OutputType> argOutputType = CommandManager.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<ServerCommandSource, DataDump.Format> argOutputFormat = CommandManager.argument("output_format", OutputFormatArgument.create()).build();

        LiteralCommandNode<ServerCommandSource> argAreaTypeAllLoaded = CommandManager.literal("all-loaded-chunks").build();
        LiteralCommandNode<ServerCommandSource> argAreaTypeBox = CommandManager.literal("box").build();
        LiteralCommandNode<ServerCommandSource> argAreaTypeChunkRadius = CommandManager.literal("chunk-radius").build();

        ArgumentCommandNode<ServerCommandSource, Integer> argChunkRadius = CommandManager.argument("chunk_radius", IntegerArgumentType.integer(1, 64)).build();

        ArgumentCommandNode<ServerCommandSource, PosArgument> argAreaCorner1 = CommandManager.argument("start_corner", Vec3ArgumentType.vec3()).build();
        ArgumentCommandNode<ServerCommandSource, PosArgument> argAreaCorner2 = CommandManager.argument("end_corner", Vec3ArgumentType.vec3()).build();

        ArgumentCommandNode<ServerCommandSource, List<String>> argNamesAllLoaded = CommandManager.argument(type.getPlural(),
                StringCollectionArgument.create(() -> type.getRegistrySupplier().get().getIds().stream().map(Identifier::toString).collect(Collectors.toList()), ""))
                .executes(ctx -> locate(type, AreaType.ALL_LOADED, ctx)).build();

        ArgumentCommandNode<ServerCommandSource, List<String>> argNamesBox = CommandManager.argument(type.getPlural(),
                StringCollectionArgument.create(() -> type.getRegistrySupplier().get().getIds().stream().map(Identifier::toString).collect(Collectors.toList()), ""))
                .executes(ctx -> locate(type, AreaType.BOX, ctx)).build();

        ArgumentCommandNode<ServerCommandSource, List<String>> argNamesChunkRadius = CommandManager.argument(type.getPlural(),
                StringCollectionArgument.create(() -> type.getRegistrySupplier().get().getIds().stream().map(Identifier::toString).collect(Collectors.toList()), ""))
                .executes(ctx -> locate(type, AreaType.CHUNK_RADIUS, ctx)).build();

        argTarget.addChild(argOutputType);
        argOutputType.addChild(argOutputFormat);
        argOutputFormat.addChild(argAreaTypeAllLoaded);
        argOutputFormat.addChild(argAreaTypeBox);
        argOutputFormat.addChild(argAreaTypeChunkRadius);

        argAreaTypeAllLoaded.addChild(argNamesAllLoaded);

        argAreaTypeBox.addChild(argAreaCorner1);
        argAreaCorner1.addChild(argAreaCorner2);
        argAreaCorner2.addChild(argNamesBox);

        argAreaTypeChunkRadius.addChild(argChunkRadius);
        argChunkRadius.addChild(argNamesChunkRadius);

        return argTarget;
    }

    private static int locate(LocateType locateType, AreaType areaType, CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException
    {
        CommandUtils.OutputType outputType = ctx.getArgument("output_type", CommandUtils.OutputType.class);
        DataDump.Format outputFormat = ctx.getArgument("output_format", DataDump.Format.class);
        @SuppressWarnings("unchecked")
        List<String> filters = ctx.getArgument(locateType.getPlural(), List.class);
        ServerCommandSource source = ctx.getSource();
        World world = TellMe.dataProvider.getWorld(source.getMinecraftServer(), CommandUtils.getDimensionFromSource(source));

        Locate locate = Locate.create(locateType, outputFormat, filters);

        switch (areaType)
        {
            case ALL_LOADED:
                locate.processChunks(TellMe.dataProvider.getLoadedChunks(world));
                break;

            case BOX:
            {
                Vec3d vecStart = CommandUtils.getVec3dFromArg(ctx, "start_corner");
                Vec3d vecEnd = CommandUtils.getVec3dFromArg(ctx, "end_corner");
                BlockPos minPos = CommandUtils.getMinCorner(vecStart, vecEnd);
                BlockPos maxPos = CommandUtils.getMaxCorner(vecStart, vecEnd);
                locate.processChunks(world, minPos, maxPos);
                break;
            }

            case CHUNK_RADIUS:
            {
                int chunkRadius = IntegerArgumentType.getInteger(ctx, "chunk_radius");
                int blockRadius = chunkRadius * 16;
                BlockPos center = CommandUtils.getBlockPosFromSource(source);
                BlockPos minPos = new BlockPos(center.getX() - blockRadius,   0, center.getZ() - blockRadius);
                BlockPos maxPos = new BlockPos(center.getX() + blockRadius, 255, center.getZ() + blockRadius);
                locate.processChunks(world, minPos, maxPos);
                break;
            }
        }

        OutputUtils.printOutput(locate.getLines(), outputType, outputFormat, "locate_" + locateType.getArgument(), source);

        return 1;
    }

    public enum AreaType
    {
        ALL_LOADED   ("all-loaded"),
        BOX          ("box"),
        CHUNK_RADIUS ("chunk-radius");

        private final String argument;

        AreaType(String argument)
        {
            this.argument = argument;
        }

        public String getArgument()
        {
            return this.argument;
        }

        public static AreaType fromArgument(String argument)
        {
            for (AreaType val : values())
            {
                if (val.argument.equalsIgnoreCase(argument))
                {
                    return val;
                }
            }

            return ALL_LOADED;
        }
    }
}
