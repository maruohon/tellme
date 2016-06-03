package fi.dy.masa.tellme.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import fi.dy.masa.tellme.TellMe;

public class BiomeInfo
{
    public static List<String> getBiomeList()
    {
        List<String> lines = new ArrayList<String>();
        Biome bgb;

        StringBuilder separator = new StringBuilder(256);
        String header = String.format("%-7s | %-24s | %-23s | %11s | %10s | %8s | %10s ",
                "biomeID", "biomeName", "waterColorMultiplier", "temperature", "temp. cat.", "rainfall", "enableSnow");
        for (int i = 0; i < 147; ++i) { separator.append("-"); }
        lines.add("Biome list:");
        lines.add(separator.toString());
        lines.add(header);
        lines.add(separator.toString());

        Iterator<Biome> iterator = Biome.REGISTRY.iterator();
        while (iterator.hasNext() == true)
        {
            bgb = iterator.next();

            if (bgb != null)
            {
                lines.add(String.format("%7d | %-24s | 0x%08X (%10d) | %11f | %10s | %8f | %10s ",
                    Biome.getIdForBiome(bgb),
                    bgb.getBiomeName(),
                    bgb.getWaterColorMultiplier(),
                    bgb.getWaterColorMultiplier(),
                    bgb.getTemperature(),
                    bgb.getTempCategory(),
                    bgb.getRainfall(),
                    bgb.getEnableSnow()));
            }
            else
            {
                lines.add(String.format("%7d | %-24s | %23s | %11s | %10s | %8s | %10s ", 0, "<none>", " ", " ", " ", " ", " "));
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
        Biome bgb = world.getBiome(pos);

        String pre = TextFormatting.YELLOW.toString();
        String aq = TextFormatting.AQUA.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        player.addChatMessage(new TextComponentString("------------- Current biome info ------------"));
        player.addChatMessage(new TextComponentString(String.format("%sBiome Name%s: %s - %sBiome ID%s: %d",
                pre, rst, bgb.getBiomeName(), pre, rst, Biome.getIdForBiome(bgb))));
        player.addChatMessage(new TextComponentString(String.format("%scanRain%s: %s, %srainfall%s: %f - %senableSnow%s: %s",
                pre, rst, bgb.canRain(), pre, rst, bgb.getRainfall(), pre, rst, bgb.getEnableSnow())));
        player.addChatMessage(new TextComponentString(String.format("%swaterColorMultiplier%s: 0x%08X (%d)",
                pre, rst, bgb.getWaterColorMultiplier(), bgb.getWaterColorMultiplier())));
        player.addChatMessage(new TextComponentString(String.format("%stemperature%s: %f, %stemp. category%s: %s%s%s",
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
