package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.ChatUtils;

public class BiomeDump
{
    public static List<String> getFormattedBiomeDump(Format format, boolean outputColors)
    {
        final boolean isClient = TellMe.proxy.isClient();
        int columns = 11;

        if (outputColors)
        {
            columns += (isClient ? 3 : 1);
        }

        DataDump biomeDump = new DataDump(columns, format);

        for (Map.Entry<ResourceLocation, Biome> entry : ForgeRegistries.BIOMES.getEntries())
        {
            Biome biome = entry.getValue();
            String id = String.valueOf(Biome.getIdForBiome(biome));
            ResourceLocation rl = entry.getKey();
            String regName = rl != null ? rl.toString() : "<null>";
            String name = TellMe.proxy.getBiomeName(biome);
            String biomeTypes = getBiomeTypesForBiome(biome);
            String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
            String validFor = getValidForString(biome);
            String temp = String.format("%5.2f", biome.getDefaultTemperature());
            String tempCat = biome.getTempCategory().toString();
            String rain = String.format("%.2f", biome.getRainfall());
            String snow = String.valueOf(biome.getEnableSnow());
            String oceanic = String.valueOf(BiomeManager.oceanBiomes.contains(biome));

            if (isClient)
            {
                if (outputColors)
                {
                    Pair<Integer, Integer> pair = TellMe.proxy.getBiomeGrassAndFoliageColors(biome);
                    String waterColor = String.format("0x%08X (%10d)", biome.getWaterColorMultiplier(), biome.getWaterColorMultiplier());
                    String grassColor = String.format("0x%08X (%10d)", pair.getLeft(), pair.getLeft());
                    String foliageColor = String.format("0x%08X (%10d)", pair.getRight(), pair.getRight());

                    biomeDump.addData(id, regName, name, temp, tempCat, rain, snow, oceanic, biomeTypes, biomeDictionaryTypes, validFor,
                                        waterColor, grassColor, foliageColor);
                }
                else
                {
                    biomeDump.addData(id, regName, name, temp, tempCat, rain, snow, oceanic, biomeTypes, biomeDictionaryTypes, validFor);
                }
            }
            else
            {
                if (outputColors)
                {
                    String waterColor = String.format("0x%08X (%10d)", biome.getWaterColorMultiplier(), biome.getWaterColorMultiplier());
                    biomeDump.addData(id, regName, name, temp, tempCat, rain, snow, oceanic, biomeTypes, biomeDictionaryTypes, validFor, waterColor);
                }
                else
                {
                    biomeDump.addData(id, regName, name, temp, tempCat, rain, snow, oceanic, biomeTypes, biomeDictionaryTypes, validFor);
                }
            }
        }

        if (isClient && outputColors)
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                                "rain", "snow", "oceanic", "BiomeType", "BiomeDictionary.Type", "Valid for",
                                "waterColorMultiplier", "grassColorMultiplier", "foliageColorMultiplier");
        }
        else if (outputColors)
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                                "rain", "snow", "oceanic", "BiomeType", "BiomeDictionary.Type", "Valid for", "waterColorMultiplier");
        }
        else
        {
            biomeDump.addTitle("ID", "Registry name", "Biome name", "temp.", "temp cat",
                                "rain", "snow", "oceanic", "BiomeType", "BiomeDictionary.Type", "Valid for");
        }

        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id
        biomeDump.setColumnProperties(3, Alignment.RIGHT, true); // temperature
        biomeDump.setColumnProperties(5, Alignment.RIGHT, true); // rainfall
        biomeDump.setColumnAlignment(6, Alignment.RIGHT); // snow

        biomeDump.setUseColumnSeparator(true);

        return biomeDump.getLines();
    }

    public static List<String> getFormattedBiomeDumpWithMobSpawns(Format format)
    {
        DataDump biomeDump = new DataDump(6, format);

        for (Map.Entry<ResourceLocation, Biome> entry : ForgeRegistries.BIOMES.getEntries())
        {
            Biome biome = entry.getValue();
            String id = String.valueOf(Biome.getIdForBiome(biome));
            ResourceLocation rl = entry.getKey();
            String regName = rl != null ? rl.toString() : "<null>";
            String name = TellMe.proxy.getBiomeName(biome);
            String biomeTypes = getBiomeTypesForBiome(biome);
            String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
            List<String> spawns = new ArrayList<>();

            for (EnumCreatureType type : EnumCreatureType.values())
            {
                List<String> tmpList = new ArrayList<>();

                // Add the spawns grouped by category and sorted alphabetically within each category
                for (SpawnListEntry spawn : biome.getSpawnableList(type))
                {
                    ResourceLocation erl = EntityList.getKey(spawn.entityClass);
                    String entName = erl != null ? erl.toString() : spawn.entityClass.getName();
                    tmpList.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, spawn.itemWeight, spawn.minGroupCount, spawn.maxGroupCount));
                }

                Collections.sort(tmpList);
                spawns.addAll(tmpList);
            }

            biomeDump.addData(id, regName, name, biomeTypes, biomeDictionaryTypes, String.join("; ", spawns));
        }

        biomeDump.addTitle("ID", "Registry name", "Biome name", "BiomeType", "BiomeDictionary.Type", "Spawns");

        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id

        biomeDump.setUseColumnSeparator(true);

        return biomeDump.getLines();
    }

    public static void printCurrentBiomeInfoToChat(EntityPlayer player)
    {
        World world = player.getEntityWorld();
        BlockPos pos = player.getPosition();
        Biome biome = world.getBiome(pos);

        String pre = TextFormatting.GREEN.toString();
        String preAqua = TextFormatting.AQUA.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        String regName = biome.getRegistryName().toString();
        String biomeTypes = getBiomeTypesForBiome(biome);
        String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
        String validFor = getValidForString(biome);
        String enableSnow = biome.getEnableSnow() ? pre + "true" : TextFormatting.RED.toString() + "false";
        String textPre = String.format("Name: %s%s%s - ID: %s%d%s - Registry name: %s",
                pre, TellMe.proxy.getBiomeName(biome), rst, pre, Biome.getIdForBiome(biome), rst, pre);

        player.sendMessage(new TextComponentString("------------- Current biome info ------------"));
        player.sendMessage(ChatUtils.getClipboardCopiableMessage(textPre, regName, rst));
        player.sendMessage(new TextComponentString(String.format("canRain: %s%s%s, rainfall: %s%f%s - enableSnow: %s%s",
                pre, biome.canRain(), rst, pre, biome.getRainfall(), rst, enableSnow, rst)));
        player.sendMessage(new TextComponentString(String.format("BiomeType: %s%s%s", preAqua, biomeTypes, rst)));
        player.sendMessage(new TextComponentString(String.format("BiomeDictionary.Type: %s%s%s", preAqua, biomeDictionaryTypes, rst)));

        if (StringUtils.isBlank(validFor) == false)
        {
            player.sendMessage(new TextComponentString(String.format("Valid for: %s%s%s", preAqua, validFor, rst)));
        }

        player.sendMessage(new TextComponentString(String.format("waterColorMultiplier: %s0x%08X (%d)%s",
                pre, biome.getWaterColorMultiplier(), biome.getWaterColorMultiplier(), rst)));
        player.sendMessage(new TextComponentString(String.format("temperature: %s%f%s, temp. category: %s%s%s",
                pre, biome.getTemperature(pos), rst, pre, biome.getTempCategory(), rst)));

        // Get the grass and foliage colors, if called on the client side
        TellMe.proxy.getCurrentBiomeInfoClientSide(player, biome);
    }

    public static List<String> getBiomeDumpIdToName(Format format)
    {
        List<IdToStringHolder> data = new ArrayList<IdToStringHolder>();
        List<String> lines = new ArrayList<String>();
        Iterator<Biome> iter = Biome.REGISTRY.iterator();

        while (iter.hasNext())
        {
            Biome biome = iter.next();
            data.add(new IdToStringHolder(Biome.getIdForBiome(biome), TellMe.proxy.getBiomeName(biome)));
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

        if (BiomeProvider.allowedBiomes.contains(biome))
        {
            strings.add("spawn");
        }

        if ((biome.getBaseHeight() > 0.0F && BiomeManager.strongHoldBiomesBlackList.contains(biome) == false) ||
             BiomeManager.strongHoldBiomes.contains(biome))
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
