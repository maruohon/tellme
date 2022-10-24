package fi.dy.masa.tellme.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class ModNameUtils
{
    private static final Map<String, String> MOD_NAMES = new HashMap<String, String>();

    public static String getModName(ResourceLocation rl)
    {
        String domain = rl.getNamespace();
        String modName = MOD_NAMES.get(domain);

        if (modName == null)
        {
            if (domain.equalsIgnoreCase("minecraft"))
            {
                modName = "Minecraft";
            }
            else
            {
                modName = domain;
            }

            modName = TextFormatting.getTextWithoutFormattingCodes(modName);
            MOD_NAMES.put(domain, modName);
        }

        return modName;
    }
}
