package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeDump
{
    public static List<String> getFormattedBiomeDump(Format format, boolean outputColors)
    {
        final boolean isClient = TellMe.isClient();
        int columns = 11;

        if (outputColors)
        {
            columns += (isClient ? 3 : 1);
        }

        DataDump biomeDump = new DataDump(columns, format);

        for (Map.Entry<ResourceLocation, Biome> entry : ForgeRegistries.BIOMES.getEntries())
        {
            Biome biome = entry.getValue();
            @SuppressWarnings("deprecation")
            String id = String.valueOf(Registry.BIOME.getId(biome));
            ResourceLocation rl = entry.getKey();
            String regName = rl != null ? rl.toString() : "<null>";
            String name = TellMe.dataProvider.getBiomeName(biome);
            String biomeTypes = getBiomeTypesForBiome(biome);
            String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
            String validFor = getValidForString(biome);
            String temp = String.format("%5.2f", biome.getDefaultTemperature());
            String tempCat = biome.getTempCategory().toString();
            Biome.RainType rainType = biome.getPrecipitation();
            String rain = rainType != Biome.RainType.NONE ? rainType.getName() : "-";
            String downfall = String.format("%.2f", biome.getDownfall());
            String oceanic = String.valueOf(BiomeManager.oceanBiomes.contains(biome));
            int waterColor = biome.getWaterColor();

            if (isClient)
            {
                if (outputColors)
                {
                    int foliageColor = TellMe.dataProvider.getFoliageColor(biome, BlockPos.ZERO);
                    int grassColor = TellMe.dataProvider.getGrassColor(biome, BlockPos.ZERO);
                    String waterColorStr = String.format("0x%08X (%10d)", waterColor, waterColor);
                    String grassColorStr = String.format("0x%08X (%10d)", grassColor, grassColor);
                    String foliageColorStr = String.format("0x%08X (%10d)", foliageColor, foliageColor);

                    biomeDump.addData(id, regName, name, temp, tempCat, rain, downfall, oceanic, biomeTypes, biomeDictionaryTypes, validFor,
                                        waterColorStr, grassColorStr, foliageColorStr);
                }
                else
                {
                    biomeDump.addData(id, regName, name, temp, tempCat, rain, downfall, oceanic, biomeTypes, biomeDictionaryTypes, validFor);
                }
            }
            else
            {
                if (outputColors)
                {
                    String waterColorStr = String.format("0x%08X (%10d)", waterColor, waterColor);
                    biomeDump.addData(id, regName, name, temp, tempCat, rain, downfall, oceanic, biomeTypes, biomeDictionaryTypes, validFor, waterColorStr);
                }
                else
                {
                    biomeDump.addData(id, regName, name, temp, tempCat, rain, downfall, oceanic, biomeTypes, biomeDictionaryTypes, validFor);
                }
            }
        }

        if (isClient && outputColors)
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                                "RainType", "Downfall", "oceanic", "BiomeType", "BiomeDictionary.Type", "Valid for",
                                "waterColorMultiplier", "grassColorMultiplier", "foliageColorMultiplier");
        }
        else if (outputColors)
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                               "RainType", "Downfall", "oceanic", "BiomeType", "BiomeDictionary.Type", "Valid for", "waterColorMultiplier");
        }
        else
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                               "RainType", "Downfall", "oceanic", "BiomeType", "BiomeDictionary.Type", "Valid for");
        }

        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id
        biomeDump.setColumnProperties(3, Alignment.RIGHT, true); // temperature
        biomeDump.setColumnProperties(5, Alignment.RIGHT, true); // rainfall
        biomeDump.setColumnAlignment(6, Alignment.RIGHT); // snow

        return biomeDump.getLines();
    }

    public static List<String> getFormattedBiomeDumpWithMobSpawns(Format format)
    {
        DataDump biomeDump = new DataDump(6, format);

        for (Map.Entry<ResourceLocation, Biome> entry : ForgeRegistries.BIOMES.getEntries())
        {
            Biome biome = entry.getValue();
            @SuppressWarnings("deprecation")
            String id = String.valueOf(Registry.BIOME.getId(biome));
            ResourceLocation rl = entry.getKey();
            String regName = rl != null ? rl.toString() : "<null>";
            String name = TellMe.dataProvider.getBiomeName(biome);
            String biomeTypes = getBiomeTypesForBiome(biome);
            String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
            List<String> spawns = new ArrayList<>();

            for (EntityClassification type : EntityClassification.values())
            {
                List<String> tmpList = new ArrayList<>();

                // Add the spawns grouped by category and sorted alphabetically within each category
                for (SpawnListEntry spawn : biome.getSpawns(type))
                {
                    ResourceLocation erl = spawn.entityType.getRegistryName();
                    String entName = erl != null ? erl.toString() : "<null>";
                    tmpList.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, spawn.itemWeight, spawn.minGroupCount, spawn.maxGroupCount));
                }

                Collections.sort(tmpList);
                spawns.addAll(tmpList);
            }

            biomeDump.addData(id, regName, name, biomeTypes, biomeDictionaryTypes, String.join("; ", spawns));
        }

        biomeDump.addTitle("ID", "Registry name", "Biome name", "BiomeType", "BiomeDictionary.Type", "Spawns");
        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id

        return biomeDump.getLines();
    }

    public static void printCurrentBiomeInfoToChat(Entity entity)
    {
        World world = entity.getEntityWorld();
        BlockPos pos = entity.getPosition();
        Biome biome = world.getBiome(pos);

        @SuppressWarnings("deprecation")
        int id = Registry.BIOME.getId(biome);
        String pre = TextFormatting.GREEN.toString();
        String preAqua = TextFormatting.AQUA.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        String name = TellMe.dataProvider.getBiomeName(biome);
        String regName = biome.getRegistryName().toString();
        String biomeTypes = getBiomeTypesForBiome(biome);
        String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
        String validFor = getValidForString(biome);
        String snowing = biome.doesSnowGenerate(world, pos) ? pre + "true" : TextFormatting.RED.toString() + "false";
        String textPre = String.format("Name: %s%s%s - ID: %s%d%s - Registry name: %s", pre, name, rst, pre, id, rst, pre);
        Biome.RainType rainType = biome.getPrecipitation();
        int waterColor = biome.getWaterColor();

        entity.sendMessage(new StringTextComponent("------------- Current biome info ------------"));
        entity.sendMessage(OutputUtils.getClipboardCopiableMessage(textPre, regName, rst));
        entity.sendMessage(new StringTextComponent(String.format("RainType: %s%s%s, downfall: %s%f%s, snows: %s%s",
                pre, rainType.getName(), rst, pre, biome.getDownfall(), rst, snowing, rst)));
        entity.sendMessage(new StringTextComponent(String.format("BiomeType: %s%s%s", preAqua, biomeTypes, rst)));
        entity.sendMessage(new StringTextComponent(String.format("BiomeDictionary.Type: %s%s%s", preAqua, biomeDictionaryTypes, rst)));

        if (StringUtils.isBlank(validFor) == false)
        {
            entity.sendMessage(new StringTextComponent(String.format("Valid for: %s%s%s", preAqua, validFor, rst)));
        }

        entity.sendMessage(new StringTextComponent(String.format("waterColorMultiplier: %s0x%08X (%d)%s",
                pre, waterColor, waterColor, rst)));
        entity.sendMessage(new StringTextComponent(String.format("temperature: %s%f%s, temp. category: %s%s%s",
                pre, biome.getTemperature(pos), rst, pre, biome.getTempCategory(), rst)));

        // Get the grass and foliage colors, if called on the client side
        TellMe.dataProvider.getCurrentBiomeInfoClientSide(entity, biome);
    }

    public static List<String> getBiomeDumpIdToName(Format format)
    {
        List<IdToStringHolder> data = new ArrayList<IdToStringHolder>();
        List<String> lines = new ArrayList<String>();

        for (Biome biome : ForgeRegistries.BIOMES)
        {
            @SuppressWarnings("deprecation")
            int id = Registry.BIOME.getId(biome);
            data.add(new IdToStringHolder(id, biome.getRegistryName().toString()));
        }

        Collections.sort(data);

        if (format == Format.CSV)
        {
            for (IdToStringHolder holder : data)
            {
                lines.add(String.valueOf(holder.getId()) + ",\"" + holder.getString() + "\"");
            }
        }
        else
        {
            for (IdToStringHolder holder : data)
            {
                lines.add(String.valueOf(holder.getId()) + " = " + holder.getString());
            }
        }

        return lines;
    }

    private static String getBiomeTypesForBiome(Biome biome)
    {
        Set<String> typeNames = new HashSet<>();

        for (BiomeType type : BiomeType.values())
        {
            ImmutableList<BiomeEntry> entries = BiomeManager.getBiomes(type);

            for (BiomeEntry entry : entries)
            {
                if (entry.biome == biome)
                {
                    typeNames.add(type.toString().toUpperCase());
                    break;
                }
            }
        }

        if (typeNames.isEmpty() == false)
        {
            List<String> typeList = new ArrayList<>(typeNames);
            Collections.sort(typeList);
            return String.join(", ", typeList);
        }

        return "";
    }

    private static String getBiomeDictionaryTypesForBiome(Biome biome)
    {
        List<String> typeStrings = new ArrayList<>();
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);

        for (BiomeDictionary.Type type : types)
        {
            typeStrings.add(type.getName().toUpperCase());
        }

        if (typeStrings.isEmpty() == false)
        {
            Collections.sort(typeStrings);
            return String.join(", ", typeStrings);
        }

        return "";
    }

    private static String getValidForString(Biome biome)
    {
        List<String> strings = new ArrayList<>();

        if (BiomeProvider.BIOMES_TO_SPAWN_IN.contains(biome))
        {
            strings.add("spawn");
        }

        for (Structure<?> structure : Feature.STRUCTURES.values())
        {
            if (biome.hasStructure(structure))
            {
                strings.add(structure.getRegistryName().toString());
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
