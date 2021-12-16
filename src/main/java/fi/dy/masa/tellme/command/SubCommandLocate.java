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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.argument.OutputFormatArgument;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.command.argument.StringCollectionArgument;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.chunkprocessor.LocateBase;
import fi.dy.masa.tellme.util.chunkprocessor.LocateBase.LocateType;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandLocate
{
    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("locate").executes(c -> printHelp(c.getSource())).build();

        subCommandRootNode.addChild(createNodes(LocateType.BLOCK));
        subCommandRootNode.addChild(createNodes(LocateType.ENTITY));
        subCommandRootNode.addChild(createNodes(LocateType.BLOCK_ENTITY));

        return subCommandRootNode;
    }

    private static int printHelp(CommandSourceStack source)
    {
        CommandUtils.sendMessage(source, "Locates Blocks, BlockEntities or Entities in the current dimension");
        CommandUtils.sendMessage(source, "Usage: /tellme locate <block | entity | block-entity> <to-chat | to-console | to-file> <ascii | csv> all-loaded-chunks <name> [name name ...]");
        CommandUtils.sendMessage(source, "Usage: /tellme locate <block | entity | block-entity> <to-chat | to-console | to-file> <ascii | csv> box <x1> <y1> <z1> <x2> <y2> <z2> <name> [name name ...]");
        CommandUtils.sendMessage(source, "Usage: /tellme locate <block | entity | block-entity> <to-chat | to-console | to-file> <ascii | csv> chunk-radius <radius> <name> [name name ...]");

        return 1;
    }

    private static LiteralCommandNode<CommandSourceStack> createNodes(LocateType type)
    {
        LiteralCommandNode<CommandSourceStack> argTarget = Commands.literal(type.getArgument()).build();

        ArgumentCommandNode<CommandSourceStack, CommandUtils.OutputType> argOutputType = Commands.argument("output_type", OutputTypeArgument.create()).build();
        ArgumentCommandNode<CommandSourceStack, DataDump.Format> argOutputFormat = Commands.argument("output_format", OutputFormatArgument.create()).build();

        LiteralCommandNode<CommandSourceStack> argAreaTypeAllLoaded = Commands.literal("all-loaded-chunks").build();
        LiteralCommandNode<CommandSourceStack> argAreaTypeBox = Commands.literal("box").build();
        LiteralCommandNode<CommandSourceStack> argAreaTypeChunkRadius = Commands.literal("chunk-radius").build();

        ArgumentCommandNode<CommandSourceStack, Integer> argChunkRadius = Commands.argument("chunk_radius", IntegerArgumentType.integer(1, 64)).build();

        ArgumentCommandNode<CommandSourceStack, Coordinates> argAreaCorner1 = Commands.argument("start_corner", Vec3Argument.vec3()).build();
        ArgumentCommandNode<CommandSourceStack, Coordinates> argAreaCorner2 = Commands.argument("end_corner", Vec3Argument.vec3()).build();

        ArgumentCommandNode<CommandSourceStack, List<String>> argNamesAllLoaded = Commands.argument(type.getPlural(),
                StringCollectionArgument.create(() -> type.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(ctx -> locate(type, AreaType.ALL_LOADED, ctx)).build();

        ArgumentCommandNode<CommandSourceStack, List<String>> argNamesBox = Commands.argument(type.getPlural(),
                StringCollectionArgument.create(() -> type.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
                .executes(ctx -> locate(type, AreaType.BOX, ctx)).build();

        ArgumentCommandNode<CommandSourceStack, List<String>> argNamesChunkRadius = Commands.argument(type.getPlural(),
                StringCollectionArgument.create(() -> type.getRegistrySupplier().get().getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toList()), ""))
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

    private static int locate(LocateType locateType, AreaType areaType, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException
    {
        CommandUtils.OutputType outputType = ctx.getArgument("output_type", CommandUtils.OutputType.class);
        DataDump.Format outputFormat = ctx.getArgument("output_format", DataDump.Format.class);
        @SuppressWarnings("unchecked")
        List<String> filters = ctx.getArgument(locateType.getPlural(), List.class);
        CommandSourceStack source = ctx.getSource();
        Level world = CommandUtils.getWorldFromCommandSource(source);

        LocateBase locate = locateType.createChunkProcessor(outputFormat, filters);

        switch (areaType)
        {
            case ALL_LOADED:
                locate.processChunks(TellMe.dataProvider.getLoadedChunks(world));
                break;

            case BOX:
            {
                Vec3 vecStart = CommandUtils.getVec3dFromArg(ctx, "start_corner");
                Vec3 vecEnd = CommandUtils.getVec3dFromArg(ctx, "end_corner");
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
