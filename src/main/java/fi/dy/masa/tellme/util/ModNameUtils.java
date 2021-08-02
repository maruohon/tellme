package fi.dy.masa.tellme.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class ModNameUtils
{
    private static final Map<String, String> MOD_IDS_TO_NAMES = new HashMap<>();

    public static String getModName(ResourceLocation rl)
    {
        return getModName(rl.getNamespace());
    }

    public static String getModName(String modId)
    {
        if (MOD_IDS_TO_NAMES.isEmpty())
        {
            for (ModInfo modInfo : ModList.get().getMods())
            {
                String modName = TextFormatting.getTextWithoutFormattingCodes(modInfo.getDisplayName());
                MOD_IDS_TO_NAMES.put(modInfo.getModId(), modName);
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
