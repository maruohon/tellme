package fi.dy.masa.tellme.network;

import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.reference.Reference;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
