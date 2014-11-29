package fi.dy.masa.tellme;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import fi.dy.masa.tellme.command.CommandTellme;
import fi.dy.masa.tellme.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
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
        configDirPath = event.getModConfigurationDirectory().getAbsolutePath().concat("/").concat(Reference.MOD_ID);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
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
