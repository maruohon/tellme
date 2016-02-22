package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import fi.dy.masa.tellme.TellMe;

public class BiomeInfo
{
    public static List<String> getBiomeList()
    {
        List<String> lines = new ArrayList<String>();
        BiomeGenBase bgb;

        StringBuilder separator = new StringBuilder(256);
        String header = String.format("%6s | %-7s | %-24s | %-23s | %-23s | %11s | %10s | %8s | %10s ",
                "index", "biomeID", "biomeName", "color", "waterColorMultiplier", "temperature", "temp. cat.", "rainfall", "enableSnow");
        for (int i = 0; i < 147; ++i) { separator.append("-"); }
        lines.add("Biome list:");
        lines.add(separator.toString());
        lines.add(header);
        lines.add(separator.toString());

        int biomeArrLen = BiomeGenBase.getBiomeGenArray().length;
        for (int i = 0; i < biomeArrLen; ++i)
        {
            bgb = BiomeGenBase.getBiome(i);

            if (bgb != null)
            {
                lines.add(String.format("%6d | %7d | %-24s | 0x%08X (%10d) | 0x%08X (%10d) | %11f | %10s | %8f | %10s ",
                    i,
                    bgb.biomeID,
                    bgb.biomeName,
                    bgb.color,
                    bgb.color,
                    bgb.getWaterColorMultiplier(),
                    bgb.getWaterColorMultiplier(),
                    bgb.temperature,
                    bgb.getTempCategory(),
                    bgb.rainfall,
                    bgb.getEnableSnow()));
            }
            else
            {
                lines.add(String.format("%6d | %7d | %-24s | %23s | %23s | %11s | %10s | %8s | %10s ", i, 0, "<none>", " ", " ", " ", " ", " ", " "));
            }
        }

        lines.add(separator.toString());
        lines.add(header);
        lines.add(separator.toString());

        return lines;
    }

    public static void printCurrentBiomeInfoToChat(EntityPlayer player)
    {
        World world = player.worldObj;
        BlockPos pos = player.getPosition();
        BiomeGenBase bgb = world.getBiomeGenForCoords(pos);

        String pre = EnumChatFormatting.YELLOW.toString();
        String aq = EnumChatFormatting.AQUA.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        player.addChatMessage(new ChatComponentText("------------- Current biome info ------------"));
        player.addChatMessage(new ChatComponentText(String.format("%sBiome Name%s: %s - %sBiome ID%s: %d",
                pre, rst, bgb.biomeName, pre, rst, bgb.biomeID)));
        player.addChatMessage(new ChatComponentText(String.format("%scanRain%s: %s, %srainfall%s: %f - %senableSnow%s: %s",
                pre, rst, bgb.canRain(), pre, rst, bgb.rainfall, pre, rst, bgb.getEnableSnow())));
        player.addChatMessage(new ChatComponentText(String.format("%scolor%s: 0x%08X (%d)",
                pre, rst, bgb.color, bgb.color)));
        player.addChatMessage(new ChatComponentText(String.format("%swaterColorMultiplier%s: 0x%08X (%d)",
                pre, rst, bgb.getWaterColorMultiplier(), bgb.getWaterColorMultiplier())));
        player.addChatMessage(new ChatComponentText(String.format("%stemperature%s: %f, %stemp. category%s: %s%s%s",
                pre, rst, bgb.getFloatTemperature(pos), pre, rst, aq, bgb.getTempCategory(), rst)));

        // Get the grass and foliage colors, if called on the client side
        TellMe.proxy.getCurrentBiomeInfoClientSide(player, bgb);
    }

    public static void printBiomeListToLogger()
    {
        List<String> lines = getBiomeList();

        for (int i = 0; i < lines.size(); ++i)
        {
            TellMe.logger.info(lines.get(i));
        }
    }
}
