package fi.dy.masa.tellme.util;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
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

        lines.add("Biome list:");
        lines.add(String.format("%6s | %-7s | %-24s | %-23s | %-23s | %14s | %14s | %11s",
                "index", "biomeID", "biomeName", "color", "waterColorMultiplier", "temperature", "rainfall", "enableSnow"));

        int biomeListLength = BiomeGenBase.getBiomeGenArray().length;
        for (int i = 0; i < biomeListLength; ++i)
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
                    bgb.waterColorMultiplier,
                    bgb.waterColorMultiplier,
                    bgb.temperature,
                    bgb.rainfall,
                    bgb.func_150559_j()));
            }
            else
            {
                lines.add(String.format("%6d | %7d | %-24s | 0x%08X (%10d) | 0x%08X (%10d) | %14f | %14f | %11s", i, 0, "<none>", 0, 0, 0, 0, 0.0f, 0.0f, ""));
            }
        }

        return lines;
    }

    public static void printCurrentBiomeInfoToChat(EntityPlayer player)
    {
        World world = player.worldObj;
        BiomeGenBase bgb = world.getBiomeGenForCoords((int)player.posX, (int)player.posZ);

        player.addChatMessage(new ChatComponentText("Current biome info:"));
        player.addChatMessage(new ChatComponentText(String.format("Name: %s - biomeID: %d", bgb.biomeName, bgb.biomeID)));
        player.addChatMessage(new ChatComponentText(String.format("%s 0x%08X (%d)", "color:", bgb.color, bgb.color)));
        player.addChatMessage(new ChatComponentText(String.format("%s 0x%08X (%d)", "waterColorMultiplier:", bgb.waterColorMultiplier, bgb.waterColorMultiplier)));
        player.addChatMessage(new ChatComponentText(String.format("temperature: %f - rainfall: %f", bgb.temperature, bgb.rainfall)));
        player.addChatMessage(new ChatComponentText(String.format("enableSnow: %s", bgb.func_150559_j())));
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
