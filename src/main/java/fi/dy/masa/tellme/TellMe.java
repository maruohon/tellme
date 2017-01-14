package fi.dy.masa.tellme;

import java.io.File;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import fi.dy.masa.tellme.command.CommandTellme;
import fi.dy.masa.tellme.config.Configs;
import fi.dy.masa.tellme.event.InteractEventHandler;
import fi.dy.masa.tellme.proxy.CommonProxy;
import fi.dy.masa.tellme.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
    guiFactory = "fi.dy.masa.tellme.config.TellMeGuiFactory",
    acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.11,1.11.2]")
public class TellMe
{
    @Mod.Instance(Reference.MOD_ID)
    public static TellMe instance;

    @SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    public static CommonProxy proxy;

    public static Logger logger;
    public static String configDirPath;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        configDirPath = new File(event.getModConfigurationDirectory(), Reference.MOD_ID).getAbsolutePath();
        Configs.loadConfigsFromFile(event.getSuggestedConfigurationFile());

        MinecraftForge.EVENT_BUS.register(new InteractEventHandler());
        proxy.registerClientCommand();
        proxy.registerEventHandlers();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandTellme());
    }
}
