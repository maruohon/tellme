package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
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
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
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

    private static Registry<Biome> getBiomeRegistry(World world)
    {
        return world.func_241828_r().func_243612_b(Registry.BIOME_KEY);
    }

    public static List<String> getFormattedBiomeDump(Format format, @Nullable World world, BiomeInfoProviderBase provider)
    {
        DataDump biomeDump = new DataDump(provider.getColumnCount(), format);

        if (world == null) { return biomeDump.getLines(); }

        BiomeDumpContext ctx = new BiomeDumpContext(world);
        Registry<Biome> registry = getBiomeRegistry(world);

        for (ResourceLocation id : registry.keySet())
        {
            Biome biome = registry.getOrDefault(id);
            provider.addLine(biomeDump, biome, id, ctx);
        }

        provider.addTitle(biomeDump);

        return biomeDump.getLines();
    }

    public static List<String> getFormattedBiomeDumpWithMobSpawns(Format format, @Nullable World world)
    {
        DataDump biomeDump = new DataDump(3, format);

        if (world == null) { return biomeDump.getLines(); }

        Registry<Biome> registry = getBiomeRegistry(world);

        for (ResourceLocation id : registry.keySet())
        {
            Biome biome = registry.getOrDefault(id);
            String intId = String.valueOf(registry.getId(biome));
            String regName = id.toString();
            //String biomeTypes = getBiomeTypesForBiome(biome);
            //String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
            List<String> spawns = new ArrayList<>();

            for (EntityClassification type : EntityClassification.values())
            {
                List<String> tmpList = new ArrayList<>();

                // Add the spawns grouped by category and sorted alphabetically within each category
                for (MobSpawnInfo.Spawners spawn : biome.func_242433_b().func_242559_a(type))
                {
                    ResourceLocation erl = spawn.field_242588_c.getRegistryName();
                    String entName = erl != null ? erl.toString() : "<null>";
                    tmpList.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, spawn.itemWeight, spawn.field_242589_d, spawn.field_242590_e));
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

    public static void printCurrentBiomeInfoToChat(PlayerEntity entity)
    {
        World world = entity.getEntityWorld();
        BlockPos pos = entity.getPosition();
        Biome biome = world.getBiome(pos);
        TextFormatting green = TextFormatting.GREEN;
        TextFormatting red = TextFormatting.RED;

        Registry<Biome> registry = getBiomeRegistry(world);
        String intId = String.valueOf(registry.getId(biome));
        String regName = registry.getKey(biome).toString();
        Biome.RainType rainType = biome.getPrecipitation();

        BiomeAmbience effects = biome.func_235089_q_();
        String strFogColor = "?";
        String strSkyColor = "?";
        String strWaterColor = "?";
        String strWaterFogColor = "?";

        String strDepth = String.valueOf(biome.getDepth());
        String strMaxSpawnChance = String.valueOf(biome.func_242433_b().func_242557_a());
        String strRainType = rainType.getName();
        String strRainfall = String.valueOf(biome.getDownfall());
        String strScale = String.valueOf(biome.getScale());
        String strTemperature = String.valueOf(biome.func_242445_k());

        try
        {
            int skyColor = ObfuscationReflectionHelper.getPrivateValue(BiomeAmbience.class, effects, "field_242523_e");
            int fogColor = ObfuscationReflectionHelper.getPrivateValue(BiomeAmbience.class, effects, "field_235205_b_");
            int waterColor = ObfuscationReflectionHelper.getPrivateValue(BiomeAmbience.class, effects, "field_235206_c_");
            int waterFogColor = ObfuscationReflectionHelper.getPrivateValue(BiomeAmbience.class, effects, "field_235207_d_");
            strFogColor = String.format("0x%08X (%d)", fogColor, fogColor);
            strSkyColor = String.format("0x%08X (%d)", skyColor, skyColor);
            strWaterColor = String.format("0x%08X (%d)", waterColor, waterColor);
            strWaterFogColor = String.format("0x%08X (%d)", waterFogColor, waterFogColor);
        }
        catch (Exception ignore) {}

        String strValidFor = getValidForString(biome);
        boolean canSnow = biome.doesSnowGenerate(world, pos);
        String strSnowing = canSnow ? "yes" : "no";

        //String biomeTypes = getBiomeTypesForBiome(biome);
        //String biomeDictionaryTypes = getBiomeDictionaryTypesForBiome(biome);
        //boolean isOceanic = BiomeManager.oceanBiomes.contains(biome);
        IFormattableTextComponent textPre = new StringTextComponent("ID: ")
                                                    .append(new StringTextComponent(intId).mergeStyle(green))
                                                    .appendString(" - Registry name: ");

        entity.sendStatusMessage(new StringTextComponent("------------- Current biome info ------------"), false);
        entity.sendStatusMessage(OutputUtils.getClipboardCopiableMessage(textPre, new StringTextComponent(regName).mergeStyle(green), new StringTextComponent("")), false);

        entity.sendStatusMessage(new StringTextComponent("Temperature: ")
                                   .append(new StringTextComponent(strTemperature).mergeStyle(green)), false);
        entity.sendStatusMessage(new StringTextComponent("RainType: ").append(new StringTextComponent(strRainType).mergeStyle(green))
                                   .appendString(", downfall: ").append(new StringTextComponent(strRainfall).mergeStyle(green))
                                   .appendString(", snows: ").append(new StringTextComponent(strSnowing).mergeStyle(canSnow ? green : red)), false);
        entity.sendStatusMessage(new StringTextComponent("Depth: ").append(new StringTextComponent(strDepth).mergeStyle(green))
                                   .appendString(", scale: ").append(new StringTextComponent(strScale).mergeStyle(green))
                                   .appendString(", max spawn chance: ").append(new StringTextComponent(strMaxSpawnChance).mergeStyle(green))
                , false);

        entity.sendStatusMessage(new StringTextComponent("Fog Color: ")
                                   .append(new StringTextComponent(strFogColor).mergeStyle(green)), false);
        entity.sendStatusMessage(new StringTextComponent("Sky Color: ")
                                   .append(new StringTextComponent(strSkyColor).mergeStyle(green)), false);
        entity.sendStatusMessage(new StringTextComponent("Water Color Multiplier: ")
                                   .append(new StringTextComponent(strWaterColor).mergeStyle(green)), false);
        entity.sendStatusMessage(new StringTextComponent("Water Fog Color: ")
                                   .append(new StringTextComponent(strWaterFogColor).mergeStyle(green)), false);

        /*
        entity.sendStatusMessage(new StringTextComponent("Biome types: ")
                                   .append(new StringTextComponent(biomeTypes).mergeStyle(green)), false);
        entity.sendStatusMessage(new StringTextComponent("Biome dictionary types: ")
                                   .append(new StringTextComponent(biomeDictionaryTypes).mergeStyle(green)), false);
        */

        if (StringUtils.isBlank(strValidFor) == false)
        {
            entity.sendStatusMessage(new StringTextComponent("Valid for: ")
                                       .append(new StringTextComponent(strValidFor).mergeStyle(TextFormatting.AQUA)), false);
        }

        // Get the grass and foliage colors, if called on the client side
        TellMe.dataProvider.getCurrentBiomeInfoClientSide(entity, biome);
    }

    public static List<String> getBiomeDumpIdToName(Format format, @Nullable World world)
    {
        List<IdToStringHolder> data = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        if (world == null) { return lines; }

        Registry<Biome> registry = getBiomeRegistry(world);

        for (ResourceLocation id : registry.keySet())
        {
            Biome biome = registry.getOrDefault(id);
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

    /*
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
    */

    private static String getValidForString(Biome biome)
    {
        List<String> strings = new ArrayList<>();

        if (biome.func_242433_b().func_242562_b())
        {
            strings.add("spawn");
        }

        for (Structure<?> structure : Structure.field_236365_a_.values())
        {
            if (biome.func_242440_e().func_242493_a(structure))
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
            String temp = String.format("%5.2f", biome.func_242445_k());
            Biome.RainType precipitation = biome.getPrecipitation();
            String precStr = precipitation != Biome.RainType.NONE ? precipitation.getName() : "-";
            String downfall = String.format("%.2f", biome.getDownfall());

            dump.addData(intId, regName, temp, precStr, downfall);
        }
    }

    public static class BiomeInfoProviderColors extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return TellMe.isClient() ? 8 : 6;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            if (TellMe.isClient())
            {
                dump.addTitle("ID", "Registry name", "Fog Color", "Sky Color", "Water Color", "Water Fog Color", "Grass Color", "Foliage Color");
            }
            else
            {
                dump.addTitle("ID", "Registry name", "Fog Color", "Sky Color", "Water Color", "Water Fog Color");
            }

            dump.setColumnProperties(0, Alignment.RIGHT, true); // id
        }

        @Override
        public void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx)
        {
            if (ctx.world == null) { return; }

            Registry<Biome> registry = getBiomeRegistry(ctx.world);
            String intId = String.valueOf(registry.getId(biome));
            String regName = id.toString();
            BiomeAmbience effects = biome.func_235089_q_();

            String strFogColor = "?";
            String strSkyColor = "?";
            String strWaterColor = "?";
            String strWaterFogColor = "?";

            try
            {
                int skyColor = ObfuscationReflectionHelper.getPrivateValue(BiomeAmbience.class, effects, "field_242523_e");
                int fogColor = ObfuscationReflectionHelper.getPrivateValue(BiomeAmbience.class, effects, "field_235205_b_");
                int waterColor = ObfuscationReflectionHelper.getPrivateValue(BiomeAmbience.class, effects, "field_235206_c_");
                int waterFogColor = ObfuscationReflectionHelper.getPrivateValue(BiomeAmbience.class, effects, "field_235207_d_");
                strFogColor = String.format("0x%08X (%d)", fogColor, fogColor);
                strSkyColor = String.format("0x%08X (%d)", skyColor, skyColor);
                strWaterColor = String.format("0x%08X (%d)", waterColor, waterColor);
                strWaterFogColor = String.format("0x%08X (%d)", waterFogColor, waterFogColor);
            }
            catch (Exception ignore) {}

            if (TellMe.isClient())
            {
                int foliageColor = TellMe.dataProvider.getFoliageColor(biome, BlockPos.ZERO);
                int grassColor = TellMe.dataProvider.getGrassColor(biome, BlockPos.ZERO);
                String grassColorStr = String.format("0x%08X (%10d)", grassColor, grassColor);
                String foliageColorStr = String.format("0x%08X (%10d)", foliageColor, foliageColor);

                dump.addData(intId, regName, strFogColor, strSkyColor, strWaterColor, strWaterFogColor, grassColorStr, foliageColorStr);
            }
            else
            {
                dump.addData(intId, regName, strFogColor, strSkyColor, strWaterColor, strWaterFogColor);
            }
        }
    }

    public static class BiomeInfoProviderValidity extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 3;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("ID", "Registry name", "Valid for");
            dump.setColumnProperties(0, Alignment.RIGHT, true); // id
        }

        @Override
        public void addLine(DataDump dump, Biome biome, ResourceLocation id, BiomeDumpContext ctx)
        {
            if (ctx.world == null) { return; }

            Registry<Biome> registry = getBiomeRegistry(ctx.world);
            String intId = String.valueOf(registry.getId(biome));
            String regName = id.toString();
            String validFor = getValidForString(biome);

            dump.addData(intId, regName, validFor);
        }
    }
}
