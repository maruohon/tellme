package fi.dy.masa.tellme.network;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import fi.dy.masa.tellme.TellMe;

public class MessageCopyToClipboard
{
    private String str;

    public MessageCopyToClipboard(FriendlyByteBuf buf)
    {
        this.str = buf.readUtf();
    }

    public MessageCopyToClipboard(String str)
    {
        this.str = str;
    }

    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeUtf(this.str);
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
                Minecraft.getInstance().keyboardHandler.setClipboard(this.str);

                if (mc.player != null)
                {
                    mc.player.displayClientMessage(new TextComponent("Copied " + this.str), true);
                }
            });

            ctx.setPacketHandled(true);
        });
    }
}
