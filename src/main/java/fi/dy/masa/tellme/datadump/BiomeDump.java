package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.feature.StructureFeature;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.mixin.IMixinWeightedPickerEntry;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class BiomeDump
{
    public static final BiomeInfoProviderBase BASIC = new BiomeInfoProviderBasic();
    public static final BiomeInfoProviderBase COLORS = new BiomeInfoProviderColors();
    public static final BiomeInfoProviderBase VALIDITY = new BiomeInfoProviderValidity();

    public static List<String> getFormattedBiomeDump(Format format, @Nullable World world, BiomeInfoProviderBase provider)
    {
        BiomeDumpContext ctx = new BiomeDumpContext(world);
        DataDump biomeDump = new DataDump(provider.getColumnCount(), format);

        for (Identifier id : Registry.BIOME.getIds())
        {
            Biome biome = Registry.BIOME.get(id);
            provider.addLine(biomeDump, biome, id, ctx);
        }

        provider.addTitle(biomeDump);
        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id

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

            for (SpawnGroup type : SpawnGroup.values())
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

    public static void printCurrentBiomeInfoToChat(PlayerEntity entity)
    {
        World world = entity.getEntityWorld();
        BlockPos pos = entity.getBlockPos();
        Biome biome = world.getBiome(pos);

        String intId = String.valueOf(Registry.BIOME.getRawId(biome));
        Formatting green = Formatting.GREEN;
        String pre = Formatting.GREEN.toString();

        String name = TellMe.dataProvider.getBiomeName(biome);
        String regName = Registry.BIOME.getId(biome).toString();
        String validFor = world instanceof ServerWorld ? getValidForString(biome, ((ServerWorld) world).getChunkManager().getChunkGenerator().getBiomeSource()) : "?";
        String snowing = biome.canSetSnow(world, pos) ? pre + "true" : Formatting.RED.toString() + "false";
        MutableText textPre = new LiteralText("Name: ")
                               .append(new LiteralText(name).formatted(green))
                               .append(" - ID: ")
                               .append(new LiteralText(intId).formatted(green))
                               .append(" - Registry name: ");
        Biome.Precipitation rainType = biome.getPrecipitation();
        int waterColor = biome.getWaterColor();

        entity.sendMessage(new LiteralText("------------- Current biome info ------------"), false);
        entity.sendMessage(OutputUtils.getClipboardCopiableMessage(textPre, new LiteralText(regName).formatted(green), new LiteralText("")), false);
        entity.sendMessage(new LiteralText("RainType: ")
                                   .append(new LiteralText(rainType.getName()).formatted(green))
                                   .append(", downfall: ")
                                   .append(new LiteralText(String.valueOf(biome.getRainfall())).formatted(green))
                                   .append(", snows: ")
                                   .append(new LiteralText(snowing).formatted(green)), false);

        if (StringUtils.isBlank(validFor) == false)
        {
            entity.sendMessage(new LiteralText("Valid for: ").append(new LiteralText(validFor).formatted(Formatting.AQUA)), false);
        }

        entity.sendMessage(new LiteralText("waterColorMultiplier: ")
                                   .append(new LiteralText(String.format("0x%08X (%d)", waterColor, waterColor)).formatted(green)), false);
        entity.sendMessage(new LiteralText("temperature: ")
                .append(new LiteralText(String.valueOf(biome.getTemperature())).formatted(green))
                .append(", temp. category: ")
                .append(new LiteralText(biome.getTemperatureGroup().toString()).formatted(green)), false);

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

        for (StructureFeature<?> feature : StructureFeature.STRUCTURES.values())
        {
            if (biome.hasStructureFeature(feature))
            {
                Identifier id = Registry.STRUCTURE_FEATURE.getId(feature);

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

    public static class BiomeDumpContext
    {
        @Nullable
        public final World world;

        public BiomeDumpContext(World world)
        {
            this.world = world;
        }
    }

    private static abstract class BiomeInfoProviderBase
    {
        public abstract int getColumnCount();

        public abstract void addTitle(DataDump dump);

        public abstract void addLine(DataDump dump, Biome biome, Identifier id, BiomeDumpContext ctx);
    }

    public static class BiomeInfoProviderBasic extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 7;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("ID", "Registry name", "Biome name", "Temp.", "Temp Cat.", "RainType", "Downfall");

            dump.setColumnProperties(3, Alignment.RIGHT, true); // temperature
            dump.setColumnProperties(5, Alignment.RIGHT, true); // raintype
            dump.setColumnAlignment(6, Alignment.RIGHT); // downfall
        }

        @Override
        public void addLine(DataDump dump, Biome biome, Identifier id, BiomeDumpContext ctx)
        {
            String intId = String.valueOf(Registry.BIOME.getRawId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);
            String temp = String.format("%5.2f", biome.getTemperature());
            String tempCat = biome.getTemperatureGroup().toString();
            Biome.Precipitation precipitation = biome.getPrecipitation();
            String precStr = precipitation != Biome.Precipitation.NONE ? precipitation.getName() : "-";
            String downfall = String.format("%.2f", biome.getRainfall());

            dump.addData(intId, regName, name, temp, tempCat, precStr, downfall);
        }
    }

    public static class BiomeInfoProviderColors extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return TellMe.isClient() ? 6 : 4;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            if (TellMe.isClient())
            {
                dump.addTitle("ID", "Registry name", "Biome name", "waterColorMultiplier", "grassColorMultiplier", "foliageColorMultiplier");
            }
            else
            {
                dump.addTitle("ID", "Registry name", "Biome name","waterColorMultiplier");
            }
        }

        @Override
        public void addLine(DataDump dump, Biome biome, Identifier id, BiomeDumpContext ctx)
        {
            String intId = String.valueOf(Registry.BIOME.getRawId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);
            int waterColor = biome.getWaterColor();
            String waterColorStr = String.format("0x%08X (%10d)", waterColor, waterColor);

            if (TellMe.isClient())
            {
                int foliageColor = TellMe.dataProvider.getFoliageColor(biome, BlockPos.ORIGIN);
                int grassColor = TellMe.dataProvider.getGrassColor(biome, BlockPos.ORIGIN);
                String grassColorStr = String.format("0x%08X (%10d)", grassColor, grassColor);
                String foliageColorStr = String.format("0x%08X (%10d)", foliageColor, foliageColor);

                dump.addData(intId, regName, name, waterColorStr, grassColorStr, foliageColorStr);
            }
            else
            {
                dump.addData(intId, regName, name, waterColorStr);
            }
        }
    }

    public static class BiomeInfoProviderValidity extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 4;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("ID", "Registry name", "Biome name", "Valid for");
        }

        @Override
        public void addLine(DataDump dump, Biome biome, Identifier id, BiomeDumpContext ctx)
        {
            String intId = String.valueOf(Registry.BIOME.getRawId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);
            String validFor = ctx.world instanceof ServerWorld ? getValidForString(biome, ((ServerWorld) ctx.world).getChunkManager().getChunkGenerator().getBiomeSource()) : "?";

            dump.addData(intId, regName, name, validFor);
        }
    }
}
