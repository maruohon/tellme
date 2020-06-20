package fi.dy.masa.tellme;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fi.dy.masa.tellme.command.CommandTellMe;
import fi.dy.masa.tellme.datadump.DataProviderBase;
import fi.dy.masa.tellme.datadump.DataProviderClient;
import fi.dy.masa.tellme.reference.Reference;
import net.fabricmc.api.ModInitializer;

public class TellMe implements ModInitializer
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);
    public static DataProviderBase dataProvider = new DataProviderBase();
    private static boolean isClient;

    @Override
    public void onInitialize()
    {
        CommandTellMe.registerArgumentTypes();
    }

    public static void setIsClient()
    {
        dataProvider = new DataProviderClient();
        isClient = true;
    }

    public static boolean isClient()
    {
        return isClient;
    }
}
