package fi.dy.masa.tellme;

import java.io.File;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.ServerCommandProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.command.ServerCommandManager;
import fi.dy.masa.malilib.config.util.ConfigUtils;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.tellme.command.ClientCommandTellme;
import fi.dy.masa.tellme.command.CommandTellme;
import fi.dy.masa.tellme.reference.Reference;

public class LiteModTellMe implements LiteMod, ServerCommandProvider
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    public static String configDirPath;

    public LiteModTellMe()
    {
    }

    @Override
    public String getName()
    {
        return Reference.MOD_NAME;
    }

    @Override
    public String getVersion()
    {
        return Reference.MOD_VERSION;
    }

    @Override
    public void init(File configPath)
    {
        configDirPath = ConfigUtils.getConfigDirectoryPath().resolve(Reference.MOD_ID).toFile().getAbsolutePath();
        Registry.CLIENT_COMMAND_HANDLER.registerCommand(new ClientCommandTellme());
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath)
    {
    }

    @Override
    public void provideCommands(ServerCommandManager commandManager)
    {
        commandManager.registerCommand(new CommandTellme());
    }
}
