package fi.dy.masa.tellme.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

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
            for (IModInfo modInfo : ModList.get().getMods())
            {
                String modName = ChatFormatting.stripFormatting(modInfo.getDisplayName());
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
