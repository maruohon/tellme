package fi.dy.masa.tellme.datadump;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import fi.dy.masa.tellme.TellMe;

public class BiomeDump extends DataDump
{
    private BiomeDump()
    {
        super(8);

        this.setSort(false);
    }

    public static List<String> getFormattedBiomeDump()
    {
        BiomeDump biomeDump = new BiomeDump();
        Iterator<Biome> iter = Biome.REGISTRY.iterator();

        while (iter.hasNext())
        {
            Biome biome = iter.next();
            String id = String.valueOf(Biome.getIdForBiome(biome));
            String regName = biome.getRegistryName().toString();
            String name = biome.getBiomeName();
            String waterColor = String.format("0x%08X (%10d)", biome.getWaterColorMultiplier(), biome.getWaterColorMultiplier());
            String temp = String.format("%5.2f", biome.getTemperature());
            String tempCat = biome.getTempCategory().toString();
            String rain = String.format("%.2f", biome.getRainfall());
            String snow = String.valueOf(biome.getEnableSnow());

            biomeDump.addData(id, regName, name, waterColor, temp, tempCat, rain, snow);
        }

        biomeDump.addTitle("ID", "Registry name", "Biome Name",
                "waterColorMultiplier", "temperature", "temp. category", "rainfall", "enableSnow");
        biomeDump.setColumnAlignment(0, Alignment.RIGHT); // id
        biomeDump.setColumnAlignment(4, Alignment.RIGHT); // temperature
        biomeDump.setColumnAlignment(6, Alignment.RIGHT); // rainfall
        biomeDump.setColumnAlignment(7, Alignment.RIGHT); // snow
        biomeDump.setUseColumnSeparator(true);

        return biomeDump.getLines();
    }

    public static void printCurrentBiomeInfoToChat(EntityPlayer player)
    {
        World world = player.getEntityWorld();
        BlockPos pos = player.getPosition();
        Biome biome = world.getBiome(pos);

        String pre = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        player.sendMessage(new TextComponentString("------------- Current biome info ------------"));
        player.sendMessage(new TextComponentString(String.format("Name: %s%s%s - ID: %s%d%s - Registry name: %s%s%s",
                pre, biome.getBiomeName(), rst, pre, Biome.getIdForBiome(biome), rst, pre, biome.getRegistryName().toString(), rst)));
        player.sendMessage(new TextComponentString(String.format("canRain: %s%s%s, rainfall: %s%f%s - enableSnow: %s%s%s",
                pre, biome.canRain(), rst, pre, biome.getRainfall(), rst, pre, biome.getEnableSnow(), rst)));
        player.sendMessage(new TextComponentString(String.format("waterColorMultiplier: %s0x%08X (%d)%s",
                pre, biome.getWaterColorMultiplier(), biome.getWaterColorMultiplier(), rst)));
        player.sendMessage(new TextComponentString(String.format("temperature: %s%f%s, temp. category: %s%s%s",
                pre, biome.getFloatTemperature(pos), rst, pre, biome.getTempCategory(), rst)));

        // Get the grass and foliage colors, if called on the client side
        TellMe.proxy.getCurrentBiomeInfoClientSide(player, biome);
    }
}
