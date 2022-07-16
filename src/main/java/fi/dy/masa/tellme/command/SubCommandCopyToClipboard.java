package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SubCommandCopyToClipboard
{
    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("copy-to-clipboard").build();

        ArgumentCommandNode<ServerCommandSource, MessageArgumentType.MessageFormat> messageNode = CommandManager.argument("message", MessageArgumentType.message())
                .executes(c -> execute(c.getSource(), MessageArgumentType.getMessage(c, "message"))).build();

        subCommandRootNode.addChild(messageNode);

        return subCommandRootNode;
    }

    private static int execute(ServerCommandSource source, Text message) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity instanceof ServerPlayerEntity)
        {
            // TODO Fabric port
            /*
            PacketHandler.INSTANCE.sendTo(new MessageCopyToClipboard(message.getString()),
                    ((ServerPlayerEntity) entity).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            */
        }
        else
        {
            source.sendFeedback(Text.literal("'/tellme copy-to-clipboard' can only be run by a player"), false);
            return -1;
        }

        return 1;
    }
}
