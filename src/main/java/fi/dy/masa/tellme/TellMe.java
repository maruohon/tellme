package fi.dy.masa.tellme;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import fi.dy.masa.tellme.command.CommandTellme;
import fi.dy.masa.tellme.event.InteractEventHandler;
import fi.dy.masa.tellme.proxy.CommonProxy;
import fi.dy.masa.tellme.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
    acceptableRemoteVersions = "*", acceptedMinecraftVersions = "1.9")
public class TellMe
{
    @Instance(Reference.MOD_ID)
    public static TellMe instance;

    @SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    public static CommonProxy proxy;

    public static Logger logger;
    public static String configDirPath;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        configDirPath = event.getModConfigurationDirectory().getAbsolutePath().concat("/" + Reference.MOD_ID);
        MinecraftForge.EVENT_BUS.register(new InteractEventHandler());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        CommandTellme.registerCommand(event);
    }
}
