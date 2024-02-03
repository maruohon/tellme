package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenVillage;

import malilib.util.MathUtils;
import malilib.util.game.wrap.GameUtils;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ChatUtils;

public class BiomeDump
{
    public static List<String> getFormattedBiomeDump(Format format, boolean outputColors)
    {
        int columns = outputColors ? 11 : 8;

        DataDump biomeDump = new DataDump(columns, format);
        biomeDump.setSort(false);
        World world = GameUtils.getClientWorld();

        if (world != null)
        {
            for (ResourceLocation key : Biome.REGISTRY.getKeys())
            {
                Biome biome = Biome.REGISTRY.getObject(key);
                String id = String.valueOf(Biome.getIdForBiome(biome));
                String regName = key.toString();
                String name = biome.getBiomeName();
                String validFor = getValidForString(world, biome);
                String temp = String.format("%5.2f", biome.getDefaultTemperature());
                String tempCat = biome.getTempCategory().toString();
                String rain = String.format("%.2f", biome.getRainfall());
                String snow = String.valueOf(biome.getEnableSnow());

                if (outputColors)
                {
                    int grassColor = getGrassColor(biome);
                    int foliageColor = getFoliageColor(biome);
                    String waterColorStr = String.format("0x%08X (%10d)", biome.getWaterColor(), biome.getWaterColor());
                    String grassColorStr = String.format("0x%08X (%10d)", grassColor, grassColor);
                    String foliageColorStr = String.format("0x%08X (%10d)", foliageColor, foliageColor);

                    biomeDump.addData(id, regName, name, temp, tempCat, rain, snow, validFor,
                                        waterColorStr, grassColorStr, foliageColorStr);
                }
                else
                {
                    biomeDump.addData(id, regName, name, temp, tempCat, rain, snow, validFor);
                }
            }
        }

        if (outputColors)
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat", "rain", "snow", "Valid for",
                                "waterColor", "grassColor", "foliageColor");
        }
        else
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat", "rain", "snow", "Valid for");
        }

        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id
        biomeDump.setColumnProperties(3, Alignment.RIGHT, true); // temperature
        biomeDump.setColumnProperties(5, Alignment.RIGHT, true); // rainfall
        biomeDump.setColumnAlignment(6, Alignment.RIGHT); // snow

        biomeDump.setUseColumnSeparator(true);

        return biomeDump.getLines();
    }

    private static int getGrassColor(Biome biome)
    {
        double temperature = MathUtils.clamp(biome.getDefaultTemperature(), 0.0F, 1.0F);
        double humidity = MathUtils.clamp(biome.getRainfall(), 0.0F, 1.0F);
        return ColorizerGrass.getGrassColor(temperature, humidity);
    }

    private static int getFoliageColor(Biome biome)
    {
        double temperature = MathUtils.clamp(biome.getDefaultTemperature(), 0.0F, 1.0F);
        double humidity = MathUtils.clamp(biome.getRainfall(), 0.0F, 1.0F);
        return ColorizerFoliage.getFoliageColor(temperature, humidity);
    }

    public static void printCurrentBiomeInfoToChat(EntityPlayer player)
    {
        World world = player.getEntityWorld();
        BlockPos pos = player.getPosition();
        Biome biome = world.getBiome(pos);

        String pre = TextFormatting.GREEN.toString();
        String preAqua = TextFormatting.AQUA.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        String regName = Biome.REGISTRY.getNameForObject(biome).toString();
        String validFor = getValidForString(world, biome);
        String enableSnow = biome.getEnableSnow() ? pre + "true" : TextFormatting.RED.toString() + "false";
        String textPre = String.format("Name: %s%s%s - ID: %s%d%s - Registry name: %s",
                pre, biome.getBiomeName(), rst, pre, Biome.getIdForBiome(biome), rst, pre);

        player.sendMessage(new TextComponentString("------------- Current biome info ------------"));
        player.sendMessage(ChatUtils.getClipboardCopiableMessage(textPre, regName, rst));
        player.sendMessage(new TextComponentString(String.format("canRain: %s%s%s, rainfall: %s%f%s - enableSnow: %s%s",
                pre, biome.canRain(), rst, pre, biome.getRainfall(), rst, enableSnow, rst)));

        if (StringUtils.isBlank(validFor) == false)
        {
            player.sendMessage(new TextComponentString(String.format("Valid for: %s%s%s", preAqua, validFor, rst)));
        }

        player.sendMessage(new TextComponentString(String.format("waterColor: %s0x%08X (%d)%s",
                pre, biome.getWaterColor(), biome.getWaterColor(), rst)));
        player.sendMessage(new TextComponentString(String.format("temperature: %s%f%s, temp. category: %s%s%s",
                pre, biome.getTemperature(pos), rst, pre, biome.getTempCategory(), rst)));

        int color = biome.getGrassColorAtPos(pos);
        player.sendMessage(new TextComponentString(String.format("Grass color: %s0x%08X%s (%s%d%s)", pre, color, rst, pre, color, rst)));

        color = biome.getFoliageColorAtPos(pos);
        player.sendMessage(new TextComponentString(String.format("Foliage color: %s0x%08X%s (%s%d%s)", pre, color, rst, pre, color, rst)));
    }

    public static List<String> getBiomeDumpIdToName(Format format)
    {
        List<IdToStringHolder> data = new ArrayList<IdToStringHolder>();
        List<String> lines = new ArrayList<String>();
        Iterator<Biome> iter = Biome.REGISTRY.iterator();

        while (iter.hasNext())
        {
            Biome biome = iter.next();
            data.add(new IdToStringHolder(Biome.getIdForBiome(biome), biome.getBiomeName()));
        }

        Collections.sort(data);

        if (format == Format.ASCII)
        {
            for (IdToStringHolder holder : data)
            {
                lines.add(String.valueOf(holder.getId()) + " = " + holder.getString());
            }
        }
        else if (format == Format.CSV)
        {
            for (IdToStringHolder holder : data)
            {
                lines.add(String.valueOf(holder.getId()) + ",\"" + holder.getString() + "\"");
            }
        }

        return lines;
    }

    private static String getValidForString(World world, Biome biome)
    {
        List<String> strings = new ArrayList<>();

        if (world.getBiomeProvider().getBiomesToSpawnIn().contains(biome))
        {
            strings.add("spawn");
        }

        // See MapGenStronghold constructor
        if (biome.getBaseHeight() > 0.0F)
        {
            strings.add("stronghold");
        }

        if (MapGenVillage.VILLAGE_SPAWN_BIOMES.contains(biome))
        {
            strings.add("village");
        }

        return String.join(", ", strings);
    }

    public static class IdToStringHolder implements Comparable<IdToStringHolder>
    {
        private final int id;
        private final String str;

        public IdToStringHolder(int id, String str)
        {
            this.id = id;
            this.str = str;
        }

        public int getId()
        {
            return this.id;
        }

        public String getString()
        {
            return this.str;
        }

        @Override
        public int compareTo(IdToStringHolder other)
        {
            if (this.id < other.id)
            {
                return -1;
            }
            else if (this.id > other.id)
            {
                return 1;
            }

            return 0;
        }
    }
}
