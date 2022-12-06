package fi.dy.masa.tellme.command;

import java.util.List;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.RayTraceUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class SubCommandLookingAt
{
    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("looking-at")
                .executes(c -> execute(OutputType.CHAT, c.getSource(), false)).build();

        ArgumentCommandNode<CommandSourceStack, OutputType> outputTypeNode = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> execute(c.getArgument("output_type", OutputType.class), c.getSource(), false)).build();

        LiteralCommandNode<CommandSourceStack> adjacentNode = Commands.literal("adjacent")
                .executes(c -> execute(c.getArgument("output_type", OutputType.class), c.getSource(), true)).build();

        subCommandRootNode.addChild(outputTypeNode);
        outputTypeNode.addChild(adjacentNode);

        return subCommandRootNode;
    }

    private static int execute(OutputType outputType, CommandSourceStack source, boolean adjacent) throws CommandSyntaxException
    {
        if ((source.getEntity() instanceof Player) == false)
        {
            throw CommandUtils.NOT_A_PLAYER_EXCEPTION.create();
        }

        handleLookedAtObject((Player) source.getEntity(), outputType, adjacent);
        return 1;
    }

    private static void handleLookedAtObject(Player player, OutputType outputType, boolean adjacent)
    {
        Level world = player.getCommandSenderWorld();
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
            player.displayClientMessage(Component.literal("Not currently looking at anything within range"), false);
        }
    }
}
