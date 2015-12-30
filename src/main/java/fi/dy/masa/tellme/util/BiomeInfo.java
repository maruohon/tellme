package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import fi.dy.masa.tellme.TellMe;

public class BiomeInfo
{
    public static ArrayList<String> getBiomeList()
    {
        ArrayList<String> lines = new ArrayList<String>();
        BiomeGenBase bgb;

        StringBuilder separator = new StringBuilder(256);
        for (int i = 0; i < 143; ++i) { separator.append("-"); }
        lines.add("Biome list:");
        lines.add(separator.toString());
        lines.add(String.format("%6s | %-7s | %-24s | %-23s | %-23s | %14s | %14s | %11s",
                "index", "biomeID", "biomeName", "color", "waterColorMultiplier", "temperature", "rainfall", "enableSnow"));
        lines.add(separator.toString());

        int biomeArrLen = BiomeGenBase.getBiomeGenArray().length;
        for (int i = 0; i < biomeArrLen; ++i)
        {
            bgb = BiomeGenBase.getBiome(i);

            if (bgb != null)
            {
                lines.add(String.format("%6d | %7d | %-24s | 0x%08X (%10d) | 0x%08X (%10d) | %14f | %14f | %11s",
                    i,
                    bgb.biomeID,
                    bgb.biomeName,
                    bgb.color,
                    bgb.color,
                    bgb.getWaterColorMultiplier(),
                    bgb.getWaterColorMultiplier(),
                    bgb.temperature,
                    bgb.rainfall,
                    bgb.getEnableSnow()));
            }
            else
            {
                lines.add(String.format("%6d | %7d | %-24s | %23s | %23s | %15s | %15s | %11s", i, 0, "<none>", " ", " ", " ", " ", " "));
            }
        }

        lines.add(separator.toString());

        return lines;
    }

    public static void printCurrentBiomeInfoToChat(EntityPlayer player)
    {
        World world = player.worldObj;
        BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
        BiomeGenBase bgb = world.getBiomeGenForCoords(pos);

        player.addChatMessage(new ChatComponentText("Current biome info:"));
        player.addChatMessage(new ChatComponentText(String.format("Name: %s - biome ID: %d", bgb.biomeName, bgb.biomeID)));
        player.addChatMessage(new ChatComponentText(String.format("color: 0x%08X (%d)", bgb.color, bgb.color)));
        player.addChatMessage(new ChatComponentText(String.format("waterColorMultiplier 0x%08X (%d)", bgb.getWaterColorMultiplier(), bgb.getWaterColorMultiplier())));
        player.addChatMessage(new ChatComponentText(String.format("temperature: %f - rainfall: %f", bgb.getFloatTemperature(pos), bgb.rainfall)));
        player.addChatMessage(new ChatComponentText(String.format("enableSnow: %s", bgb.getEnableSnow())));
        player.addChatMessage(new ChatComponentText(String.format("Temperature Category: %s", bgb.getTempCategory())));
        // These are client-side only:
        //player.addChatMessage(new ChatComponentText(String.format("grass color 0x%08X (%d)", bgb.getModdedBiomeGrassColor(bgb.getGrassColorAtPos(pos)), bgb.getModdedBiomeGrassColor(bgb.getGrassColorAtPos(pos)))));
        //player.addChatMessage(new ChatComponentText(String.format("foliage color 0x%08X (%d)", bgb.getModdedBiomeFoliageColor(bgb.getFoliageColorAtPos(pos)), bgb.getModdedBiomeFoliageColor(bgb.getFoliageColorAtPos(pos)))));
    }

    public static void printBiomeListToLogger()
    {
        ArrayList<String> lines = getBiomeList();

        for (int i = 0; i < lines.size(); ++i)
        {
            TellMe.logger.info(lines.get(i));
        }
    }
}
