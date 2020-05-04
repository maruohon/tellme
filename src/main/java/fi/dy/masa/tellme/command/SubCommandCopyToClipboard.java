package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import fi.dy.masa.tellme.network.MessageCopyToClipboard;
import fi.dy.masa.tellme.network.PacketHandler;
import net.minecraftforge.fml.network.NetworkDirection;

public class SubCommandCopyToClipboard
{
    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("copy-to-clipboard").build();

        ArgumentCommandNode<CommandSource, MessageArgument.Message> messageNode = Commands.argument("message", MessageArgument.message())
                .executes(c -> execute(c.getSource(), MessageArgument.getMessage(c, "message"))).build();

        subCommandRootNode.addChild(messageNode);

        return subCommandRootNode;
    }

    private static int execute(CommandSource source, ITextComponent message) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity instanceof ServerPlayerEntity)
        {
            PacketHandler.INSTANCE.sendTo(new MessageCopyToClipboard(message.getString()),
                    ((ServerPlayerEntity) entity).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
        else
        {
            source.sendFeedback(new StringTextComponent("'/tellme copy-to-clipboard' can only be run by a player"), false);
            return -1;
        }

        return 1;
    }
}
