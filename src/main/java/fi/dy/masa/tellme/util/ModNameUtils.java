package fi.dy.masa.tellme.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

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
                ModContainer mc = getModContainer(domain);
                modName = mc != null ? mc.getName() : "Unknown";

                if (modName == null)
                {
                    modName = "Unknown";
                }
            }

            MOD_NAMES.put(domain, modName);
        }

        return modName;
    }

    public static ModContainer getModContainer(String modId)
    {
        Map<String, ModContainer> modList = Loader.instance().getIndexedModList();
        ModContainer modContainer = modList.get(modId);

        if (modContainer == null)
        {
            for (ModContainer mc : modList.values())
            {
                if (mc.getModId().equalsIgnoreCase(modId))
                {
                    return mc;
                }
            }
        }

        return modContainer;
    }
}
