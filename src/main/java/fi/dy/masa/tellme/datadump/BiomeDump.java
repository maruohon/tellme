package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class BiomeDump
{
    public static final BiomeInfoProviderBase BASIC = new BiomeInfoProviderBasic();
    public static final BiomeInfoProviderBase COLORS = new BiomeInfoProviderColors();
    public static final BiomeInfoProviderBase TYPES = new BiomeInfoProviderTypes();

    private static Registry<Biome> getBiomeRegistry(Level world)
    {
        return world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
    }

    private static ResourceKey<Biome> getBiomeKey(Biome biome, Level world)
    {
        return world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(biome).orElse(null);
    }

    public static List<String> getFormattedBiomeDump(Format format, @Nullable Level world, BiomeInfoProviderBase provider)
    {
        DataDump biomeDump = new DataDump(provider.getColumnCount(), format);

        if (world == null) { return biomeDump.getLines(); }

        BiomeDumpContext ctx = new BiomeDumpContext(world);
        Registry<Biome> registry = getBiomeRegistry(world);

        for (ResourceLocation id : registry.keySet())
        {
            Biome biome = registry.get(id);
            provider.addLine(biomeDump, biome, id, ctx);
        }

        provider.addTitle(biomeDump);

        return biomeDump.getLines();
    }

    public static List<String> getFormattedBiomeDumpWithMobSpawns(Format format, @Nullable Level world)
    {
        DataDump biomeDump = new DataDump(3, format);

        if (world == null) { return biomeDump.getLines(); }

        Registry<Biome> registry = getBiomeRegistry(world);

        for (ResourceLocation id : registry.keySet())
        {
            Biome biome = registry.get(id);
            String intId = String.valueOf(registry.getId(biome));
            String regName = id.toString();
            //String biomeTypes = getBiomeTypesForBiome(biome);
            //String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
            List<String> spawns = new ArrayList<>();

            for (MobCategory type : MobCategory.values())
            {
                List<String> tmpList = new ArrayList<>();

                // Add the spawns grouped by category and sorted alphabetically within each category
                for (MobSpawnSettings.SpawnerData spawn : biome.getMobSettings().getMobs(type).unwrap())
                {
                    ResourceLocation erl = spawn.type.getRegistryName();
                    String entName = erl != null ? erl.toString() : "<null>";
                    int weight = spawn.getWeight().asInt();
                    tmpList.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, weight, spawn.minCount, spawn.maxCount));
                }

                Collections.sort(tmpList);
                spawns.addAll(tmpList);
            }

            biomeDump.addData(intId, regName, String.join("; ", spawns));
        }

        biomeDump.addTitle("ID", "Registry name", "Spawns");
        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id

        return biomeDump.getLines();
    }

    public static void printCurrentBiomeInfoToChat(Player entity)
    {
        Level world = entity.getCommandSenderWorld();
        BlockPos pos = entity.blockPosition();
        Biome biome = world.getBiome(pos).value();
        ChatFormatting green = ChatFormatting.GREEN;
        ChatFormatting red = ChatFormatting.RED;

        Registry<Biome> registry = getBiomeRegistry(world);
        String intId = String.valueOf(registry.getId(biome));
        String regName = registry.getKey(biome).toString();
        Biome.Precipitation rainType = biome.getPrecipitation();

        BiomeSpecialEffects effects = biome.getSpecialEffects();
        int skyColor = effects.getSkyColor();
        int fogColor = effects.getFogColor();
        int waterColor = effects.getWaterColor();
        int waterFogColor = effects.getWaterFogColor();
        String strFogColor = String.format("0x%08X (%d)", fogColor, fogColor);
        String strSkyColor = String.format("0x%08X (%d)", skyColor, skyColor);
        String strWaterColor = String.format("0x%08X (%d)", waterColor, waterColor);
        String strWaterFogColor = String.format("0x%08X (%d)", waterFogColor, waterFogColor);

        String strMaxSpawnChance = String.valueOf(biome.getMobSettings().getCreatureProbability());
        String strRainType = rainType.getName();
        String strRainfall = String.valueOf(biome.getDownfall());
        String strTemperature = String.valueOf(biome.getBaseTemperature());

        boolean canSnow = biome.shouldSnow(world, pos);
        String strSnowing = canSnow ? "yes" : "no";

        String biomeTypes = getBiomeTypesForBiome(world, biome);
        String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(getBiomeKey(biome, world));
        //boolean isOceanic = BiomeManager.oceanBiomes.contains(biome);
        MutableComponent textPre = new TextComponent("ID: ")
                                                    .append(new TextComponent(intId).withStyle(green))
                                                    .append(" - Registry name: ");

        entity.displayClientMessage(new TextComponent("------------- Current biome info ------------"), false);
        entity.displayClientMessage(OutputUtils.getClipboardCopiableMessage(textPre, new TextComponent(regName).withStyle(green), new TextComponent("")), false);

        entity.displayClientMessage(new TextComponent("Temperature: ")
                                   .append(new TextComponent(strTemperature).withStyle(green)), false);
        entity.displayClientMessage(new TextComponent("RainType: ").append(new TextComponent(strRainType).withStyle(green))
                                   .append(", downfall: ").append(new TextComponent(strRainfall).withStyle(green))
                                   .append(", snows: ").append(new TextComponent(strSnowing).withStyle(canSnow ? green : red)), false);
        entity.displayClientMessage(new TextComponent("Max spawn chance: ").append(new TextComponent(strMaxSpawnChance).withStyle(green)), false);

        entity.displayClientMessage(new TextComponent("Fog Color: ")
                                   .append(new TextComponent(strFogColor).withStyle(green)), false);
        entity.displayClientMessage(new TextComponent("Sky Color: ")
                                   .append(new TextComponent(strSkyColor).withStyle(green)), false);
        entity.displayClientMessage(new TextComponent("Water Color Multiplier: ")
                                   .append(new TextComponent(strWaterColor).withStyle(green)), false);
        entity.displayClientMessage(new TextComponent("Water Fog Color: ")
                                   .append(new TextComponent(strWaterFogColor).withStyle(green)), false);

        entity.displayClientMessage(new TextComponent("Biome types: ")
                                   .append(new TextComponent(biomeTypes).withStyle(green)), false);

        entity.displayClientMessage(new TextComponent("Biome dictionary types: ")
                                   .append(new TextComponent(biomeDictionaryTypes).withStyle(green)), false);

        // Get the grass and foliage colors, if called on the client side
        TellMe.dataProvider.getCurrentBiomeInfoClientSide(entity, biome);
    }

    public static List<String> getBiomeDumpIdToName(Format format, @Nullable Level world)
    {
        List<IdToStringHolder> data = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        if (world == null) { return lines; }

        Registry<Biome> registry = getBiomeRegistry(world);

        for (ResourceLocation id : registry.keySet())
        {
            Biome biome = registry.get(id);
            int intId = registry.getId(biome);
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

    private static String getBiomeTypesForBiome(Level world, Biome biome)
    {
        Set<String> typeNames = new HashSet<>();
        Registry<Biome> registry = getBiomeRegistry(world);

        for (BiomeManager.BiomeType type : BiomeManager.BiomeType.values())
        {
            ImmutableList<BiomeManager.BiomeEntry> entries = BiomeManager.getBiomes(type);

            for (BiomeManager.BiomeEntry entry : entries)
            {
                if (registry.get(entry.getKey()) == biome)
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

    private static String getBiomeDictionaryTypesForBiome(ResourceKey<Biome> biomeKey)
    {
        List<String> typeStrings = new ArrayList<>();
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biomeKey);

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
        public final Level world;

        public BiomeDumpContext(@Nullable Level world)
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
            return 5;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("ID", "Registry name", "Temp.", "RainType", "Downfall");

            dump.setColumnProperties(0, Alignment.RIGHT, true); // id
            dump.setColumnProperties(2, Alignment.RIGHT, true); // temperature
            dump.setColumnProperties(3, Alignment.RIGHT, true); // raintype
            dump.setColumnAlignment(4, Alignment.RIGHT); // downfall
        }

        @Override
        public void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx)
        {
            if (ctx.world == null) { return; }

            Registry<Biome> registry = getBiomeRegistry(ctx.world);
            String intId = String.valueOf(registry.getId(biome));
            String regName = id.toString();
            String temp = String.format("%5.2f", biome.getBaseTemperature());
            Biome.Precipitation precipitation = biome.getPrecipitation();
            String precStr = precipitation != Biome.Precipitation.NONE ? precipitation.getName() : "-";
            String downfall = String.format("%.2f", biome.getDownfall());

            dump.addData(intId, regName, temp, precStr, downfall);
        }
    }

    public static class BiomeInfoProviderTypes extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 7;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("ID", "Registry name", "Temp.", "RainType", "Downfall", "BiomeTypes", "BiomeDictionary Types");

            dump.setColumnProperties(0, Alignment.RIGHT, true); // id
            dump.setColumnProperties(2, Alignment.RIGHT, true); // temperature
            dump.setColumnProperties(3, Alignment.RIGHT, true); // raintype
            dump.setColumnAlignment(4, Alignment.RIGHT); // downfall
        }

        @Override
        public void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx)
        {
            if (ctx.world == null) { return; }

            Registry<Biome> registry = getBiomeRegistry(ctx.world);
            String intId = String.valueOf(registry.getId(biome));
            String regName = id.toString();
            String temp = String.format("%5.2f", biome.getBaseTemperature());
            Biome.Precipitation precipitation = biome.getPrecipitation();
            String precStr = precipitation != Biome.Precipitation.NONE ? precipitation.getName() : "-";
            String downfall = String.format("%.2f", biome.getDownfall());
            String biomeTypes = getBiomeTypesForBiome(ctx.world, biome);
            String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(getBiomeKey(biome, ctx.world));

            dump.addData(intId, regName, temp, precStr, downfall, biomeTypes, biomeDictionaryTypes);
        }
    }

    public static class BiomeInfoProviderColors extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 8;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("ID", "Registry name", "Fog Color", "Sky Color", "Water Color", "Water Fog Color", "Grass Color", "Foliage Color");
            dump.setColumnProperties(0, Alignment.RIGHT, true); // id
        }

        @Override
        public void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx)
        {
            if (ctx.world == null) { return; }

            Registry<Biome> registry = getBiomeRegistry(ctx.world);
            String intId = String.valueOf(registry.getId(biome));
            String regName = id.toString();
            BiomeSpecialEffects effects = biome.getSpecialEffects();

            int skyColor = effects.getSkyColor();
            int fogColor = effects.getFogColor();
            int waterColor = effects.getWaterColor();
            int waterFogColor = effects.getWaterFogColor();
            int foliageColor = biome.getFoliageColor();
            int grassColor = biome.getGrassColor(0, 0);

            String strFogColor = String.format("0x%08X (%d)", fogColor, fogColor);
            String strSkyColor = String.format("0x%08X (%d)", skyColor, skyColor);
            String strWaterColor = String.format("0x%08X (%d)", waterColor, waterColor);
            String strWaterFogColor = String.format("0x%08X (%d)", waterFogColor, waterFogColor);
            String grassColorStr = String.format("0x%08X (%10d)", grassColor, grassColor);
            String foliageColorStr = String.format("0x%08X (%10d)", foliageColor, foliageColor);

            dump.addData(intId, regName, strFogColor, strSkyColor, strWaterColor, strWaterFogColor, grassColorStr, foliageColorStr);
        }
    }
}
