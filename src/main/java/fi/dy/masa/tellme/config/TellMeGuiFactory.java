package fi.dy.masa.tellme.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.DefaultGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import fi.dy.masa.tellme.reference.Reference;

public class TellMeGuiFactory extends DefaultGuiFactory
{
    public TellMeGuiFactory()
    {
        super(Reference.MOD_ID, getTitle());
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parent)
    {
        return new GuiConfig(parent, getConfigElements(), Reference.MOD_ID, false, false, getTitle());
    }

    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> configElements = new ArrayList<IConfigElement>();

        configElements.addAll(new ConfigElement(Configs.config.getCategory(Configs.CATEGORY_GENERIC)).getChildElements());

        return configElements;
    }

    private static String getTitle()
    {
        return GuiConfig.getAbridgedConfigPath(Configs.configurationFile.toString());
    }
}
