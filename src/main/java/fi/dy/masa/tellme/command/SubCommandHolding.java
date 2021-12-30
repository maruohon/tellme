package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;
import fi.dy.masa.tellme.command.argument.OutputTypeArgument;
import fi.dy.masa.tellme.util.ItemInfo;

public class SubCommandHolding
{
    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("holding").build();
        ArgumentCommandNode<CommandSourceStack, OutputType> outputTypeNode = Commands.argument("output_type", OutputTypeArgument.create())
                .executes(c -> execute(c.getSource(), c.getArgument("output_type", OutputType.class))).build();

        subCommandRootNode.addChild(outputTypeNode);

        return subCommandRootNode;
    }

    private static int execute(CommandSourceStack source, OutputType outputType) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity instanceof Player)
        {
            handleHeldObject((Player) entity, outputType);
            return 1;
        }

        throw CommandUtils.NOT_A_PLAYER_EXCEPTION.create();
    }

    private static void handleHeldObject(Player player, OutputType outputType)
    {
        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty() == false)
        {
            ItemInfo.printItemInfo(player, stack, outputType);
        }
    }
}
