package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.registries.ForgeRegistries;
import fi.dy.masa.tellme.TellMe;
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

        for (Map.Entry<ResourceLocation, Biome> entry : ForgeRegistries.BIOMES.getEntries())
        {
            provider.addLine(biomeDump, entry.getValue(), entry.getKey(), ctx);
        }

        provider.addTitle(biomeDump);

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

    public static void printCurrentBiomeInfoToChat(PlayerEntity entity)
    {
        World world = entity.getEntityWorld();
        BlockPos pos = entity.func_233580_cy_();
        Biome biome = world.getBiome(pos);

        @SuppressWarnings("deprecation")
        String intId = String.valueOf(Registry.BIOME.getId(biome));
        TextFormatting green = TextFormatting.GREEN;
        TextFormatting red = TextFormatting.RED;

        String name = TellMe.dataProvider.getBiomeName(biome);
        String regName = ForgeRegistries.BIOMES.getKey(biome).toString();
        String biomeTypes = getBiomeTypesForBiome(biome);
        String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
        boolean isOceanic = BiomeManager.oceanBiomes.contains(biome);
        boolean snowing = biome.doesSnowGenerate(world, pos);
        String validFor = getValidForString(biome);
        IFormattableTextComponent textPre = new StringTextComponent("Name: ")
                               .func_230529_a_(new StringTextComponent(name).func_240699_a_(green))
                               .func_240702_b_(" - ID: ")
                               .func_230529_a_(new StringTextComponent(intId).func_240699_a_(green))
                               .func_240702_b_(" - Registry name: ");
        Biome.RainType rainType = biome.getPrecipitation();

        entity.sendStatusMessage(new StringTextComponent("------------- Current biome info ------------"), false);
        entity.sendStatusMessage(OutputUtils.getClipboardCopiableMessage(textPre, new StringTextComponent(regName).func_240699_a_(green), new StringTextComponent("")), false);
        entity.sendStatusMessage(new StringTextComponent("RainType: ")
                                   .func_230529_a_(new StringTextComponent(rainType.getName()).func_240699_a_(green))
                                   .func_240702_b_(", Downfall: ")
                                   .func_230529_a_(new StringTextComponent(String.valueOf(biome.getDownfall())).func_240699_a_(green))
                                   .func_240702_b_(", Snows: ")
                                   .func_230529_a_(new StringTextComponent(snowing ? "yes" : "no").func_240699_a_(snowing ? green : red))
                                   .func_240702_b_(", Oceanic: ")
                                   .func_230529_a_(new StringTextComponent(isOceanic ? "yes" : "no").func_240699_a_(isOceanic ? green : red)), false);

        entity.sendStatusMessage(new StringTextComponent("Temperature: ")
                                   .func_230529_a_(new StringTextComponent(String.valueOf(biome.getTemperature(pos))).func_240699_a_(green))
                                   .func_240702_b_(", Temp. category: ")
                                   .func_230529_a_(new StringTextComponent(biome.getTempCategory().toString()).func_240699_a_(green)), false);
        entity.sendStatusMessage(new StringTextComponent("Biome types: ")
                                   .func_230529_a_(new StringTextComponent(biomeTypes).func_240699_a_(green)), false);
        entity.sendStatusMessage(new StringTextComponent("Biome dictionary types: ")
                                   .func_230529_a_(new StringTextComponent(biomeDictionaryTypes).func_240699_a_(green)), false);

        if (StringUtils.isBlank(validFor) == false)
        {
            entity.sendStatusMessage(new StringTextComponent("Valid for: ")
                                       .func_230529_a_(new StringTextComponent(validFor).func_240699_a_(TextFormatting.AQUA)), false);
        }

        // Get the grass and foliage colors, if called on the client side
        TellMe.dataProvider.getCurrentBiomeInfoClientSide(entity, biome);
    }

    public static List<String> getBiomeDumpIdToName(Format format)
    {
        List<IdToStringHolder> data = new ArrayList<>();
        List<String> lines = new ArrayList<>();

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

        for (Structure<?> structure : Structure.field_236365_a_.values())
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

    public static class BiomeDumpContext
    {
        @Nullable
        public final World world;

        public BiomeDumpContext(@Nullable World world)
        {
            this.world = world;
        }
    }

    private static abstract class BiomeInfoProviderBase
    {
        public abstract int getColumnCount();

        public abstract void addTitle(DataDump dump);

        public abstract void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx);
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

            dump.setColumnProperties(0, Alignment.RIGHT, true); // id
            dump.setColumnProperties(3, Alignment.RIGHT, true); // temperature
            dump.setColumnProperties(5, Alignment.RIGHT, true); // raintype
            dump.setColumnAlignment(6, Alignment.RIGHT); // downfall
        }

        @Override
        public void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx)
        {
            @SuppressWarnings("deprecation")
            String intId = String.valueOf(Registry.BIOME.getId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);
            String temp = String.format("%5.2f", biome.getDefaultTemperature());
            String tempCat = biome.getTempCategory().toString();
            Biome.RainType precipitation = biome.getPrecipitation();
            String precStr = precipitation != Biome.RainType.NONE ? precipitation.getName() : "-";
            String downfall = String.format("%.2f", biome.getDownfall());

            dump.addData(intId, regName, name, temp, tempCat, precStr, downfall);
        }
    }

    public static class BiomeInfoProviderColors extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return TellMe.isClient() ? 6 : 3;
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
                dump.addTitle("ID", "Registry name", "Biome name");
            }

            dump.setColumnProperties(0, Alignment.RIGHT, true); // id
        }

        @Override
        public void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx)
        {
            @SuppressWarnings("deprecation")
            String intId = String.valueOf(Registry.BIOME.getId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);

            TellMe.dataProvider.addBiomeInfoWithColors(dump, biome, intId, regName, name);
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
            dump.setColumnProperties(0, Alignment.RIGHT, true); // id
        }

        @Override
        public void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx)
        {
            @SuppressWarnings("deprecation")
            String intId = String.valueOf(Registry.BIOME.getId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);
            String validFor = ctx.world != null ? getValidForString(biome) : "?";

            dump.addData(intId, regName, name, validFor);
        }
    }
}
