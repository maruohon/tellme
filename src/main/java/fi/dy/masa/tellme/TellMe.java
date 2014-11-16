package fi.dy.masa.tellme;

import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import fi.dy.masa.tellme.event.BiomeEvents;
import fi.dy.masa.tellme.reference.Reference;
import fi.dy.masa.tellme.util.BiomeInfo;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class TellMe
{
    @Instance(Reference.MOD_ID)
    public static TellMe instance;

    //@SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    //public static IProxy proxy;
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeEvents());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        logger.info("WorldType.worldTypes.length: " + WorldType.worldTypes.length);
        BiomeInfo.printBiomeList();
    }
}
