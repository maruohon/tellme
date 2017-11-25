package fi.dy.masa.tellme.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.network.MessageCopyToClipboard;
import fi.dy.masa.tellme.network.PacketHandler;

public class SubCommandCopyToClipboard extends SubCommand
{
    public SubCommandCopyToClipboard(CommandTellme baseCommand)
    {
        super(baseCommand);
    }

    @Override
    public String getName()
    {
        return "copy-to-clipboard";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (sender instanceof EntityPlayerMP)
        {
            PacketHandler.INSTANCE.sendTo(new MessageCopyToClipboard(String.join(" ", args)), (EntityPlayerMP) sender);
        }
        else
        {
            this.sendMessage(sender, this.getName() + " can only be run by a player");
        }
    }
}
