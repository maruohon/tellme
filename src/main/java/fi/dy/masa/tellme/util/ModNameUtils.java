package fi.dy.masa.tellme.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ModNameUtils
{
    private static final Map<String, String> MOD_IDS_TO_NAMES = new HashMap<>();

    public static String getModName(Identifier rl)
    {
        String modId = rl.getNamespace();

        if (MOD_IDS_TO_NAMES.isEmpty())
        {
            for (net.fabricmc.loader.api.ModContainer container : net.fabricmc.loader.api.FabricLoader.getInstance().getAllMods())
            {
                if (container.getMetadata().getId().equals(modId))
                {
                    String modName = Formatting.strip(container.getMetadata().getName());
                    MOD_IDS_TO_NAMES.put(modId, modName);
                }
            }
        }

        String modName = MOD_IDS_TO_NAMES.get(modId);

        if (modName == null)
        {
            if (modId.equalsIgnoreCase("minecraft"))
            {
                modName = "Minecraft";
            }
            else
            {
                modName = "Unknown";
            }

            MOD_IDS_TO_NAMES.put(modId, modName);
        }

        return modName;
    }
}
