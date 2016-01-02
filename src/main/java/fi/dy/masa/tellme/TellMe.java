package fi.dy.masa.tellme;

import org.apache.logging.log4j.Logger;
import fi.dy.masa.tellme.command.CommandTellme;
import fi.dy.masa.tellme.event.InteractEventHandler;
import fi.dy.masa.tellme.reference.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
    acceptableRemoteVersions = "*", acceptedMinecraftVersions = "1.8,1.8.8,1.8.9")
public class TellMe
{
    @Instance(Reference.MOD_ID)
    public static TellMe instance;

    //@SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    //public static IProxy proxy;

    public static Logger logger;
    public static String configDirPath;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        configDirPath = event.getModConfigurationDirectory().getAbsolutePath().concat("/" + Reference.MOD_ID);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new InteractEventHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        CommandTellme.registerCommand(event);
    }
}
