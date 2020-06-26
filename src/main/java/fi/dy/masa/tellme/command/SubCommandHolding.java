package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.util.ItemInfo;

public class SubCommandHolding
{
    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("holding").build();
        ArgumentCommandNode<ServerCommandSource, OutputType> outputTypeNode = CommandManager.argument("output_type", OutputTypeArgument.create())
                .executes(c -> execute(c.getSource(), c.getArgument("output_type", OutputType.class))).build();

        subCommandRootNode.addChild(outputTypeNode);

        return subCommandRootNode;
    }

    private static int execute(ServerCommandSource source, OutputType outputType) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity instanceof PlayerEntity)
        {
            handleHeldObject((PlayerEntity) entity, outputType);
            return 1;
        }

        throw CommandUtils.NOT_A_PLAYER_EXCEPTION.create();
    }

    private static void handleHeldObject(PlayerEntity player, OutputType outputType)
    {
        ItemStack stack = player.getMainHandStack();

        if (stack.isEmpty() == false)
        {
            ItemInfo.printItemInfo(player, stack, outputType);
        }
    }
}
