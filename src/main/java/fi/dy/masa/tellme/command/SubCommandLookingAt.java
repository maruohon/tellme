package fi.dy.masa.tellme.command;

import java.util.List;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
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
    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("looking-at")
                .executes(c -> execute(OutputType.CHAT, c.getSource(), false)).build();

        ArgumentCommandNode<CommandSource, OutputType> outputTypeNode = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> execute(c.getArgument("output_type", OutputType.class), c.getSource(), false)).build();

        LiteralCommandNode<CommandSource> adjacentNode = Commands.literal("adjacent")
                .executes(c -> execute(c.getArgument("output_type", OutputType.class), c.getSource(), true)).build();

        subCommandRootNode.addChild(outputTypeNode);
        outputTypeNode.addChild(adjacentNode);

        return subCommandRootNode;
    }

    private static int execute(OutputType outputType, CommandSource source, boolean adjacent) throws CommandSyntaxException
    {
        if (source.getEntity() == null)
        {
            throw CommandUtils.NOT_AN_ENTITY_EXCEPTION.create();
        }

        handleLookedAtObject(source.getEntity(), outputType, adjacent);
        return 1;
    }

    private static void handleLookedAtObject(Entity entity, OutputType outputType, boolean adjacent)
    {
        World world = entity.getEntityWorld();
        RayTraceResult trace = RayTraceUtils.getRayTraceFromEntity(world, entity, true, 10d);
        List<String> lines = null;
        String fileName = "looking_at_";

        if (trace.getType() == RayTraceResult.Type.BLOCK)
        {
            lines = BlockInfo.getBlockInfoFromRayTracedTarget(world, entity, trace, adjacent, outputType == OutputType.CHAT);
            fileName += "block";
        }
        else if (trace.getType() == RayTraceResult.Type.ENTITY)
        {
            lines = EntityInfo.getFullEntityInfo(((EntityRayTraceResult) trace).getEntity(), outputType == OutputType.CHAT);
            fileName += "entity";
        }

        if (lines != null && lines.isEmpty() == false)
        {
            OutputUtils.printOutput(lines, outputType, DataDump.Format.ASCII, fileName, entity);
        }
        else
        {
            entity.sendMessage(new StringTextComponent("Not currently looking at anything within range"));
        }
    }
}
