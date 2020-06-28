package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.feature.StructureFeature;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.mixin.IMixinBiome;
import fi.dy.masa.tellme.mixin.IMixinBiomeAdditionsSound;
import fi.dy.masa.tellme.mixin.IMixinBiomeEffects;
import fi.dy.masa.tellme.mixin.IMixinBiomeMoodSound;
import fi.dy.masa.tellme.mixin.IMixinMusicSound;
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
                    tmpList.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, ((IMixinWeightedPickerEntry) spawn).tellmeGetWeight(), spawn.minGroupSize, spawn.maxGroupSize));
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

        String name = TellMe.dataProvider.getBiomeName(biome);
        String regName = Registry.BIOME.getId(biome).toString();

        Biome.Precipitation rainType = biome.getPrecipitation();
        BiomeEffects effects = biome.getEffects();
        int skyColor = ((IMixinBiome) biome).tellmeGetSkyColor();
        int fogColor = ((IMixinBiomeEffects) effects).tellmeGetFogColor();
        int waterColor = ((IMixinBiomeEffects) effects).tellmeGetWaterColor();
        int waterFogColor = ((IMixinBiomeEffects) effects).tellmeGetWaterFogColor();

        String strDepth = String.valueOf(biome.getDepth());
        String strMaxSpawnChance = String.valueOf(biome.getMaxSpawnChance());
        String strRainType = rainType.getName();
        String strRainfall = String.valueOf(biome.getRainfall());
        String strScale = String.valueOf(biome.getScale());
        String strTemperature = String.valueOf(biome.getTemperature());
        String strTempGroup = biome.getTemperatureGroup().toString();

        String strFogColor = String.format("0x%08X (%d)", fogColor, fogColor);
        String strSkyColor = String.format("0x%08X (%d)", skyColor, skyColor);
        String strWaterColor = String.format("0x%08X (%d)", waterColor, waterColor);
        String strWaterFogColor = String.format("0x%08X (%d)", waterFogColor, waterFogColor);

        String strValidFor = world instanceof ServerWorld ? getValidForString(biome, ((ServerWorld) world).getChunkManager().getChunkGenerator().getBiomeSource()) : "?";
        boolean canSnow = biome.canSetSnow(world, pos);
        String strSnowing = canSnow ? "true" : "false";

        MutableText textPre = new LiteralText("Name: ").append(new LiteralText(name).formatted(green))
                               .append(" - ID: ").append(new LiteralText(intId).formatted(green))
                               .append(" - Registry name: ");

        entity.sendMessage(new LiteralText("------------- Current biome info ------------"), false);
        entity.sendMessage(OutputUtils.getClipboardCopiableMessage(textPre, new LiteralText(regName).formatted(green), new LiteralText("")), false);

        entity.sendMessage(new LiteralText("Temperature: ")
                                   .append(new LiteralText(strTemperature).formatted(green))
                                   .append(", temp. group: ")
                                   .append(new LiteralText(strTempGroup).formatted(green)), false);
        entity.sendMessage(new LiteralText("RainType: ").append(new LiteralText(strRainType).formatted(green))
                                   .append(", downfall: ").append(new LiteralText(strRainfall).formatted(green))
                                   .append(", snows: ").append(new LiteralText(strSnowing).formatted(canSnow ? green : Formatting.RED)), false);
        entity.sendMessage(new LiteralText("Depth: ").append(new LiteralText(strDepth).formatted(green))
                                   .append(", scale: ").append(new LiteralText(strScale).formatted(green))
                                   .append(", max spawn chance: ").append(new LiteralText(strMaxSpawnChance).formatted(green))
                , false);

        entity.sendMessage(new LiteralText("Fog Color: ")
                                   .append(new LiteralText(strFogColor).formatted(green)), false);
        entity.sendMessage(new LiteralText("Sky Color: ")
                                   .append(new LiteralText(strSkyColor).formatted(green)), false);
        entity.sendMessage(new LiteralText("waterColorMultiplier: ")
                                   .append(new LiteralText(strWaterColor).formatted(green)), false);
        entity.sendMessage(new LiteralText("Water Fog Color: ")
                                   .append(new LiteralText(strWaterFogColor).formatted(green)), false);

        entity.sendMessage(getMusicInfo(((IMixinBiomeEffects) effects).tellmeGetMusic()), false);
        entity.sendMessage(getAdditionsSoundInfo(((IMixinBiomeEffects) effects).tellmeGetAdditionsSound()), false);
        entity.sendMessage(getLoopSoundInfo(((IMixinBiomeEffects) effects).tellmeGetLoopSound()), false);
        entity.sendMessage(getMoodSoundInfo(((IMixinBiomeEffects) effects).tellmeGetMoodSound()), false);

        // Get the grass and foliage colors, if called on the client side
        TellMe.dataProvider.getCurrentBiomeInfoClientSide(entity, biome);

        if (StringUtils.isBlank(strValidFor) == false)
        {
            entity.sendMessage(new LiteralText("Valid for: ").append(new LiteralText(strValidFor).formatted(Formatting.AQUA)), false);
        }
    }

    public static MutableText getMusicInfo(Optional<MusicSound> optional)
    {
        LiteralText text = new LiteralText("Music: ");
        Formatting green = Formatting.GREEN;

        if (optional.isPresent())
        {
            IMixinMusicSound accessor = (IMixinMusicSound) optional.get();
            Identifier id = Registry.SOUND_EVENT.getId(accessor.tellmeGetSound());
            String name = id != null ? id.toString() : "?";

            return text.append(new LiteralText(name).formatted(green))
                       .append(", min delay: ").append(new LiteralText(String.valueOf(accessor.tellmeGetMinDelay())).formatted(green))
                       .append(", max delay: ").append(new LiteralText(String.valueOf(accessor.tellmeGetMaxDelay())).formatted(green))
                       .append(", can stop: ").append(new LiteralText(String.valueOf(accessor.tellmeGetCanStop())).formatted(green));
        }

        return text.append(new LiteralText("-").formatted(Formatting.RED));
    }

    public static MutableText getAdditionsSoundInfo(Optional<BiomeAdditionsSound> optional)
    {
        LiteralText text = new LiteralText("Additions Sound: ");
        Formatting green = Formatting.GREEN;

        if (optional.isPresent())
        {
            IMixinBiomeAdditionsSound accessor = (IMixinBiomeAdditionsSound) optional.get();
            Identifier id = Registry.SOUND_EVENT.getId(accessor.tellmeGetSound());
            String name = id != null ? id.toString() : "?";

            return text.append(new LiteralText(name).formatted(green))
                       .append(", chance: ").append(new LiteralText(String.valueOf(accessor.tellmeGetPlayChance())).formatted(green));
        }

        return text.append(new LiteralText("-").formatted(Formatting.RED));
    }

    public static MutableText getLoopSoundInfo(Optional<SoundEvent> optional)
    {
        LiteralText text = new LiteralText("Loop Sound: ");

        if (optional.isPresent())
        {
            Identifier id = Registry.SOUND_EVENT.getId(optional.get());
            String name = id != null ? id.toString() : "?";

            return text.append(new LiteralText(name).formatted(Formatting.GREEN));
        }

        return text.append(new LiteralText("-").formatted(Formatting.RED));
    }

    public static MutableText getMoodSoundInfo(Optional<BiomeMoodSound> optional)
    {
        LiteralText text = new LiteralText("Mood Sound: ");
        Formatting green = Formatting.GREEN;

        if (optional.isPresent())
        {
            IMixinBiomeMoodSound accessor = (IMixinBiomeMoodSound) optional.get();
            Identifier id = Registry.SOUND_EVENT.getId(accessor.tellmeGetSound());
            String name = id != null ? id.toString() : "?";

            return text.append(new LiteralText(name).formatted(green))
                       .append(", delay: ").append(new LiteralText(String.valueOf(accessor.tellmeGetCultivationTicks())).formatted(green))
                       .append(", range: ").append(new LiteralText(String.valueOf(accessor.tellmeGetSpawnRange())).formatted(green))
                       .append(", extra distance: ").append(new LiteralText(String.valueOf(accessor.tellmeGetExtraDistance())).formatted(green));
        }

        return text.append(new LiteralText("-").formatted(Formatting.RED));
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
            return TellMe.isClient() ? 9 : 7;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            if (TellMe.isClient())
            {
                dump.addTitle("ID", "Registry name", "Biome name", "Fog Color", "Sky Color", "Water Color", "Water Fog Color", "Grass Color", "Foliage Color");
            }
            else
            {
                dump.addTitle("ID", "Registry name", "Biome name", "Fog Color", "Sky Color", "Water Color", "Water Fog Color");
            }
        }

        @Override
        public void addLine(DataDump dump, Biome biome, Identifier id, BiomeDumpContext ctx)
        {
            String intId = String.valueOf(Registry.BIOME.getRawId(biome));
            String regName = id.toString();
            String name = TellMe.dataProvider.getBiomeName(biome);
            BiomeEffects effects = biome.getEffects();
            int skyColor = ((IMixinBiome) biome).tellmeGetSkyColor();
            int fogColor = ((IMixinBiomeEffects) effects).tellmeGetFogColor();
            int waterColor = ((IMixinBiomeEffects) effects).tellmeGetWaterColor();
            int waterFogColor = ((IMixinBiomeEffects) effects).tellmeGetWaterFogColor();
            String strFogColor = String.format("0x%08X (%d)", fogColor, fogColor);
            String strSkyColor = String.format("0x%08X (%d)", skyColor, skyColor);
            String strWaterColor = String.format("0x%08X (%d)", waterColor, waterColor);
            String strWaterFogColor = String.format("0x%08X (%d)", waterFogColor, waterFogColor);

            if (TellMe.isClient())
            {
                int foliageColor = TellMe.dataProvider.getFoliageColor(biome, BlockPos.ORIGIN);
                int grassColor = TellMe.dataProvider.getGrassColor(biome, BlockPos.ORIGIN);
                String grassColorStr = String.format("0x%08X (%10d)", grassColor, grassColor);
                String foliageColorStr = String.format("0x%08X (%10d)", foliageColor, foliageColor);

                dump.addData(intId, regName, name, strFogColor, strSkyColor, strWaterColor, strWaterFogColor, grassColorStr, foliageColorStr);
            }
            else
            {
                dump.addData(intId, regName, name, strFogColor, strSkyColor, strWaterColor, strWaterFogColor);
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
