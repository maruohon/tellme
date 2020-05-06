package fi.dy.masa.tellme;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fi.dy.masa.tellme.command.CommandReloadConfig;
import fi.dy.masa.tellme.command.CommandTellMe;
import fi.dy.masa.tellme.config.Configs;
import fi.dy.masa.tellme.datadump.DataProviderBase;
import fi.dy.masa.tellme.datadump.DataProviderClient;
import fi.dy.masa.tellme.event.InteractEventHandler;
import fi.dy.masa.tellme.network.PacketHandler;
import fi.dy.masa.tellme.reference.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;

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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onFingerPrintViolation);

        // Make sure the mod being absent on the other network side does not cause
        // the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (incoming, isNetwork) -> true));

        MinecraftForge.EVENT_BUS.register(new InteractEventHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);

        Configs.loadConfig(FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + ".toml"));
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

    private void onServerStarting(final FMLServerStartingEvent event)
    {
        CommandReloadConfig.register(event.getCommandDispatcher());
        CommandTellMe.registerServerCommand(event.getCommandDispatcher());
    }

    private void onFingerPrintViolation(final FMLFingerprintViolationEvent event)
    {
        // Not running in a dev environment
        if (event.isDirectory() == false)
        {
            logger.warn("*********************************************************************************************");
            logger.warn("*****                                    WARNING                                        *****");
            logger.warn("*****                                                                                   *****");
            logger.warn("*****   The signature of the mod file '{}' does not match the expected fingerprint!     *****", event.getSource().getName());
            logger.warn("*****   This might mean that the mod file has been tampered with!                       *****");
            logger.warn("*****   If you did not download the mod {} directly from Curse/CurseForge,       *****", Reference.MOD_NAME);
            logger.warn("*****   or using one of the well known launchers, and you did not                       *****");
            logger.warn("*****   modify the mod file at all yourself, then it's possible,                        *****");
            logger.warn("*****   that it may contain malware or other unwanted things!                           *****");
            logger.warn("*********************************************************************************************");
        }
    }

    public static boolean isClient()
    {
        return isClient;
    }
}
