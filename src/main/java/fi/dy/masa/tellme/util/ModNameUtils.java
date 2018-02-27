package fi.dy.masa.tellme.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;

public class ModNameUtils
{
    private static final Map<String, String> MOD_NAMES = new HashMap<String, String>();

    public static String getModName(ResourceLocation rl)
    {
        String domain = rl.getResourceDomain();
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

            MOD_NAMES.put(domain, modName);
        }

        return modName;
    }
}
