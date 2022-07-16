package fi.dy.masa.tellme.command;

import java.util.List;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.RayTraceUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandLookingAt
{
    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("looking-at")
                .executes(c -> execute(OutputType.CHAT, c.getSource(), false)).build();

        ArgumentCommandNode<ServerCommandSource, OutputType> outputTypeNode = CommandManager.argument("output_type", OutputTypeArgument.create())
                .executes(c -> execute(c.getArgument("output_type", OutputType.class), c.getSource(), false)).build();

        LiteralCommandNode<ServerCommandSource> adjacentNode = CommandManager.literal("adjacent")
                .executes(c -> execute(c.getArgument("output_type", OutputType.class), c.getSource(), true)).build();

        subCommandRootNode.addChild(outputTypeNode);
        outputTypeNode.addChild(adjacentNode);

        return subCommandRootNode;
    }

    private static int execute(OutputType outputType, ServerCommandSource source, boolean adjacent) throws CommandSyntaxException
    {
        if ((source.getEntity() instanceof PlayerEntity) == false)
        {
            throw CommandUtils.NOT_A_PLAYER_EXCEPTION.create();
        }

        handleLookedAtObject((PlayerEntity) source.getEntity(), outputType, adjacent);
        return 1;
    }

    private static void handleLookedAtObject(PlayerEntity player, OutputType outputType, boolean adjacent)
    {
        World world = player.getEntityWorld();
        HitResult trace = RayTraceUtils.getRayTraceFromEntity(world, player, true, 10d);
        List<String> lines = null;
        String fileName = "looking_at_";

        if (trace.getType() == HitResult.Type.BLOCK)
        {
            lines = BlockInfo.getBlockInfoFromRayTracedTarget(world, player, trace, adjacent, outputType == OutputType.CHAT);
            fileName += "block";
        }
        else if (trace.getType() == HitResult.Type.ENTITY)
        {
            lines = EntityInfo.getFullEntityInfo(((EntityHitResult) trace).getEntity(), outputType == OutputType.CHAT);
            fileName += "entity";
        }

        if (lines != null && lines.isEmpty() == false)
        {
            OutputUtils.printOutput(lines, outputType, DataDump.Format.ASCII, fileName, player);
        }
        else
        {
            player.sendMessage(Text.literal("Not currently looking at anything within range"), false);
        }
    }
}
