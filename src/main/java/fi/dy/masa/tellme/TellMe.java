package fi.dy.masa.tellme;

import net.ornithemc.osl.entrypoints.api.ModInitializer;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.lifecycle.api.server.MinecraftServerEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.command.ServerCommandManager;

import malilib.config.util.ConfigUtils;
import malilib.registry.Registry;
import fi.dy.masa.tellme.command.ClientCommandTellme;
import fi.dy.masa.tellme.command.CommandTellme;
import fi.dy.masa.tellme.reference.Reference;

public class TellMe implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    public static String configDirPath;

    @Override
    public void init()
    {
        MinecraftClientEvents.READY.register(mc -> onClientReady());
        MinecraftServerEvents.READY.register(srv -> ((ServerCommandManager) srv.getCommandManager()).registerCommand(new CommandTellme()));
        Registry.CLIENT_COMMAND_HANDLER.registerCommand(new ClientCommandTellme());
    }

    private static void onClientReady()
    {
        configDirPath = ConfigUtils.getConfigDirectory().resolve(Reference.MOD_ID).toFile().getAbsolutePath();
    }
}
