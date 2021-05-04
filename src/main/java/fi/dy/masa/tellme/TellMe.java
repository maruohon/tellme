package fi.dy.masa.tellme;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import fi.dy.masa.tellme.command.CommandReloadConfig;
import fi.dy.masa.tellme.command.CommandTellMe;
import fi.dy.masa.tellme.config.Configs;
import fi.dy.masa.tellme.datadump.DataProviderBase;
import fi.dy.masa.tellme.datadump.DataProviderClient;
import fi.dy.masa.tellme.event.InteractEventHandler;
import fi.dy.masa.tellme.network.PacketHandler;
import fi.dy.masa.tellme.reference.Reference;

@Mod(Reference.MOD_ID)
public class TellMe
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);
    public static DataProviderBase dataProvider;
    private static boolean isClient;

    public TellMe()
    {
        dataProvider = new DataProviderBase();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.COMMON_CONFIG, Reference.MOD_ID + ".toml");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        // Make sure the mod being absent on the other network side does not cause
        // the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (incoming, isNetwork) -> true));

        MinecraftForge.EVENT_BUS.register(new InteractEventHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        Configs.loadConfig(FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + ".toml"));

        CommandTellMe.registerArgumentTypes();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        Configs.setGlobalConfigDirAndLoadConfigs(FMLPaths.CONFIGDIR.get().toFile());
        PacketHandler.registerMessages();
    }

    private void onClientSetup(final FMLClientSetupEvent event)
    {
        isClient = true;
        dataProvider = new DataProviderClient();
    }

    private void onRegisterCommands(final RegisterCommandsEvent event)
    {
        CommandReloadConfig.register(event.getDispatcher());
        CommandTellMe.registerServerCommand(event.getDispatcher());
    }

    public static boolean isClient()
    {
        return isClient;
    }
}
