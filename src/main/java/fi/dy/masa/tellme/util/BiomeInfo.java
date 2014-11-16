package fi.dy.masa.tellme.util;

import net.minecraft.world.biome.BiomeGenBase;
import fi.dy.masa.tellme.TellMe;

public class BiomeInfo
{
    public static void printBiomeList()
    {
        TellMe.logger.info("Biome list:");
        TellMe.logger.info("index | biomeID | biomeName | color | waterColorMultiplier | temperature");
        BiomeGenBase[] biomeList = BiomeGenBase.getBiomeGenArray();

        if (biomeList != null)
        {
            int len = biomeList.length;
            for (int i = 0; i < len; ++i)
            {
                if (biomeList[i] != null)
                {
                    TellMe.logger.info(String.format("%3d | %3d | %-20s | 0x%08X | 0x%08X | %2.6f",
                        i,
                        biomeList[i].biomeID,
                        biomeList[i].biomeName,
                        biomeList[i].color,
                        biomeList[i].waterColorMultiplier,
                        biomeList[i].temperature));
                }
                else
                {
                	TellMe.logger.info(String.format("%3d | %3d | %-20s | 0x%08X | 0x%08X | %2.6f", i, 0, "<none>", 0, 0, 0.0f));
                }
            }
        }
    }
}
