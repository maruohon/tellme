package fi.dy.masa.tellme.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import fi.dy.masa.tellme.reference.Reference;

public class PacketHandler
{
    public static final String PROTOCOL = "0.1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
                                                    .named(new ResourceLocation(Reference.MOD_ID, "msgs"))
                                                    .clientAcceptedVersions((str) -> true)
                                                    .serverAcceptedVersions((str) -> true)
                                                    .networkProtocolVersion(() -> PROTOCOL).simpleChannel();

    public static void registerMessages()
    {
        INSTANCE.registerMessage(0, MessageCopyToClipboard.class, MessageCopyToClipboard::toBytes, MessageCopyToClipboard::new, MessageCopyToClipboard::handle);
    }
}
