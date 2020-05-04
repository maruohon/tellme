package fi.dy.masa.tellme.network;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import fi.dy.masa.tellme.TellMe;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageCopyToClipboard
{
    private String str;

    public MessageCopyToClipboard(PacketBuffer buf)
    {
        this.str = buf.readString();
    }

    public MessageCopyToClipboard(String str)
    {
        this.str = str;
    }

    public void toBytes(PacketBuffer buf)
    {
        buf.writeString(this.str);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            if (ctx.getDirection() != NetworkDirection.PLAY_TO_CLIENT)
            {
                TellMe.logger.error("Wrong side in MessageSyncSlot: " + ctx.getDirection());
                return;
            }

            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null)
            {
                TellMe.logger.error("Player was null in MessageCopyToClipboard");
                return;
            }

            mc.execute(() -> {
                Minecraft.getInstance().keyboardListener.setClipboardString(this.str);

                if (mc.player != null)
                {
                    mc.player.sendStatusMessage(new StringTextComponent("Copied " + this.str), true);
                }
            });

            ctx.setPacketHandled(true);
        });
    }
}
