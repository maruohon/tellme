package fi.dy.masa.tellme.network;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.tellme.TellMe;
import io.netty.buffer.ByteBuf;

public class MessageCopyToClipboard implements IMessage
{
    private String str;

    public MessageCopyToClipboard()
    {
    }

    public MessageCopyToClipboard(String str)
    {
        this.str = str;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.str = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.str);
    }

    public static class Handler implements IMessageHandler<MessageCopyToClipboard, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageCopyToClipboard message, MessageContext ctx)
        {
            if (ctx.side != Side.CLIENT)
            {
                TellMe.logger.error("Wrong side in MessageCopyToClipboard: " + ctx.side);
                return null;
            }

            Minecraft mc = FMLClientHandler.instance().getClient();

            if (mc == null)
            {
                TellMe.logger.error("Minecraft was null in MessageCopyToClipboard");
                return null;
            }

            mc.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    processMessage(message);
                }
            });

            return null;
        }

        protected void processMessage(final MessageCopyToClipboard message)
        {
            if (Desktop.isDesktopSupported())
            {
                StringSelection stringSelection = new StringSelection(message.str);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                FMLClientHandler.instance().getClient().player.sendStatusMessage(new TextComponentString("Copied " + message.str), true);
            }
        }
    }
}
