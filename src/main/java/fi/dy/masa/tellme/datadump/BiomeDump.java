package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.StructureFeature;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.mixin.IMixinWeightedPickerEntry;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class BiomeDump
{
    public static List<String> getFormattedBiomeDump(Format format, boolean outputColors, @Nullable World world)
    {
        final boolean isClient = TellMe.isClient();
        int columns = 8;

        if (outputColors)
        {
            columns += (isClient ? 3 : 1);
        }

        DataDump biomeDump = new DataDump(columns, format);

        for (Identifier id : Registry.BIOME.getIds())
        {
            Biome biome = Registry.BIOME.get(id);
            String intId = String.valueOf(Registry.BIOME.getRawId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);
            String validFor = world != null ? getValidForString(biome, world.getChunkManager().getChunkGenerator().getBiomeSource()) : "?";
            String temp = String.format("%5.2f", biome.getTemperature());
            String tempCat = biome.getTemperatureGroup().toString();
            Biome.Precipitation rainType = biome.getPrecipitation();
            String rain = rainType != Biome.Precipitation.NONE ? rainType.getName() : "-";
            String downfall = String.format("%.2f", biome.getRainfall());
            int waterColor = biome.getWaterColor();

            if (isClient)
            {
                if (outputColors)
                {
                    int foliageColor = TellMe.dataProvider.getFoliageColor(biome, BlockPos.ORIGIN);
                    int grassColor = TellMe.dataProvider.getGrassColor(biome, BlockPos.ORIGIN);
                    String waterColorStr = String.format("0x%08X (%10d)", waterColor, waterColor);
                    String grassColorStr = String.format("0x%08X (%10d)", grassColor, grassColor);
                    String foliageColorStr = String.format("0x%08X (%10d)", foliageColor, foliageColor);

                    biomeDump.addData(intId, regName, name, temp, tempCat, rain, downfall, validFor,
                                        waterColorStr, grassColorStr, foliageColorStr);
                }
                else
                {
                    biomeDump.addData(intId, regName, name, temp, tempCat, rain, downfall, validFor);
                }
            }
            else
            {
                if (outputColors)
                {
                    String waterColorStr = String.format("0x%08X (%10d)", waterColor, waterColor);
                    biomeDump.addData(intId, regName, name, temp, tempCat, rain, downfall, validFor, waterColorStr);
                }
                else
                {
                    biomeDump.addData(intId, regName, name, temp, tempCat, rain, downfall, validFor);
                }
            }
        }

        if (isClient && outputColors)
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                                "RainType", "Downfall", "Valid for",
                                "waterColorMultiplier", "grassColorMultiplier", "foliageColorMultiplier");
        }
        else if (outputColors)
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                               "RainType", "Downfall", "Valid for", "waterColorMultiplier");
        }
        else
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                               "RainType", "Downfall", "Valid for");
        }

        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id
        biomeDump.setColumnProperties(3, Alignment.RIGHT, true); // temperature
        biomeDump.setColumnProperties(5, Alignment.RIGHT, true); // raintype
        biomeDump.setColumnAlignment(6, Alignment.RIGHT); // downfall

        return biomeDump.getLines();
    }

    public static List<String> getFormattedBiomeDumpWithMobSpawns(Format format)
    {
        DataDump biomeDump = new DataDump(4, format);

        for (Identifier id : Registry.BIOME.getIds())
        {
            Biome biome = Registry.BIOME.get(id);
            String intId = String.valueOf(Registry.BIOME.getRawId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);
            List<String> spawns = new ArrayList<>();

            for (EntityCategory type : EntityCategory.values())
            {
                List<String> tmpList = new ArrayList<>();

                // Add the spawns grouped by category and sorted alphabetically within each category
                for (Biome.SpawnEntry spawn : biome.getEntitySpawnList(type))
                {
                    Identifier erl = Registry.ENTITY_TYPE.getId(spawn.type);
                    String entName = erl.toString();
                    tmpList.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, ((IMixinWeightedPickerEntry) spawn).getWeight(), spawn.minGroupSize, spawn.maxGroupSize));
                }

                Collections.sort(tmpList);
                spawns.addAll(tmpList);
            }

            biomeDump.addData(intId, regName, name, String.join("; ", spawns));
        }

        biomeDump.addTitle("ID", "Registry name", "Biome name", "Spawns");
        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id

        return biomeDump.getLines();
    }

    public static void printCurrentBiomeInfoToChat(Entity entity)
    {
        World world = entity.getEntityWorld();
        BlockPos pos = entity.getBlockPos();
        Biome biome = world.getBiome(pos);

        int intId = Registry.BIOME.getRawId(biome);
        String pre = Formatting.GREEN.toString();
        String preAqua = Formatting.AQUA.toString();
        String rst = Formatting.RESET.toString() + Formatting.WHITE.toString();

        String name = TellMe.dataProvider.getBiomeName(biome);
        String regName = Registry.BIOME.getId(biome).toString();
        String validFor = getValidForString(biome, world.getChunkManager().getChunkGenerator().getBiomeSource());
        String snowing = biome.canSetSnow(world, pos) ? pre + "true" : Formatting.RED.toString() + "false";
        String textPre = String.format("Name: %s%s%s - ID: %s%d%s - Registry name: %s", pre, name, rst, pre, intId, rst, pre);
        Biome.Precipitation rainType = biome.getPrecipitation();
        int waterColor = biome.getWaterColor();

        entity.sendMessage(new LiteralText("------------- Current biome info ------------"));
        entity.sendMessage(OutputUtils.getClipboardCopiableMessage(textPre, regName, rst));
        entity.sendMessage(new LiteralText(String.format("RainType: %s%s%s, downfall: %s%f%s, snows: %s%s",
                pre, rainType.getName(), rst, pre, biome.getRainfall(), rst, snowing, rst)));

        if (StringUtils.isBlank(validFor) == false)
        {
            entity.sendMessage(new LiteralText(String.format("Valid for: %s%s%s", preAqua, validFor, rst)));
        }

        entity.sendMessage(new LiteralText(String.format("waterColorMultiplier: %s0x%08X (%d)%s",
                pre, waterColor, waterColor, rst)));
        entity.sendMessage(new LiteralText(String.format("temperature: %s%f%s, temp. category: %s%s%s",
                pre, biome.getTemperature(pos), rst, pre, biome.getTemperatureGroup(), rst)));

        // Get the grass and foliage colors, if called on the client side
        TellMe.dataProvider.getCurrentBiomeInfoClientSide(entity, biome);
    }

    public static List<String> getBiomeDumpIdToName(Format format)
    {
        List<IdToStringHolder> data = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        for (Identifier id : Registry.BIOME.getIds())
        {
            Biome biome = Registry.BIOME.get(id);
            int intId = Registry.BIOME.getRawId(biome);
            data.add(new IdToStringHolder(intId, id.toString()));
        }

        Collections.sort(data);

        if (format == Format.CSV)
        {
            for (IdToStringHolder holder : data)
            {
                lines.add(holder.getId() + ",\"" + holder.getString() + "\"");
            }
        }
        else
        {
            for (IdToStringHolder holder : data)
            {
                lines.add(holder.getId() + " = " + holder.getString());
            }
        }

        return lines;
    }

    private static String getValidForString(Biome biome, BiomeSource biomeSource)
    {
        List<String> strings = new ArrayList<>();

        if (biomeSource.getSpawnBiomes().contains(biome))
        {
            strings.add("spawn");
        }

        for (StructureFeature<?> feature : Feature.STRUCTURES.values())
        {
            if (biome.hasStructureFeature(feature))
            {
                Identifier id = Registry.FEATURE.getId(feature);

                if (id != null)
                {
                    strings.add(id.toString());
                }
            }
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
