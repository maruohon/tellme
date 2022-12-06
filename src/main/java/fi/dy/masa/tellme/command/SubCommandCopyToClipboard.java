package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;

import fi.dy.masa.tellme.network.MessageCopyToClipboard;
import fi.dy.masa.tellme.network.PacketHandler;

public class SubCommandCopyToClipboard
{
    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralCommandNode<CommandSourceStack> subCommandRootNode = Commands.literal("copy-to-clipboard").build();

        ArgumentCommandNode<CommandSourceStack, MessageArgument.Message> messageNode = Commands.argument("message", MessageArgument.message())
                .executes(c -> execute(c.getSource(), MessageArgument.getMessage(c, "message"))).build();

        subCommandRootNode.addChild(messageNode);

        return subCommandRootNode;
    }

    private static int execute(CommandSourceStack source, Component message) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity instanceof ServerPlayer)
        {
            PacketHandler.INSTANCE.sendTo(new MessageCopyToClipboard(message.getString()),
                                          ((ServerPlayer) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
        else
        {
            source.sendSuccess(Component.literal("'/tellme copy-to-clipboard' can only be run by a player"), false);
            return -1;
        }

        return 1;
    }
}
