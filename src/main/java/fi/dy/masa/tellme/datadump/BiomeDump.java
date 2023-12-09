package fi.dy.masa.tellme.datadump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.SpawnSettings;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.OutputUtils;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class BiomeDump
{
    public static final BiomeInfoProviderBase BASIC = new BiomeInfoProviderBasic();
    public static final BiomeInfoProviderBase COLORS = new BiomeInfoProviderColors();
    public static final BiomeInfoProviderBase TAGS = new BiomeInfoProviderTags();

    public static List<String> getFormattedBiomeDump(Format format, @Nullable World world, BiomeInfoProviderBase provider)
    {
        DataDump biomeDump = new DataDump(provider.getColumnCount(), format);

        if (world == null) { return biomeDump.getLines(); }

        BiomeDumpContext ctx = new BiomeDumpContext(world);
        Registry<Biome> registry = world.getRegistryManager().get(RegistryKeys.BIOME);

        for (Identifier id : registry.getIds())
        {
            Biome biome = registry.get(id);
            provider.addLine(biomeDump, biome, id, ctx);
        }

        provider.addTitle(biomeDump);
        biomeDump.setColumnProperties(0, Alignment.RIGHT, true); // id

        return biomeDump.getLines();
    }

    public static List<String> getFormattedBiomeDumpWithMobSpawns(Format format, @Nullable World world)
    {
        DataDump biomeDump = new DataDump(3, format);

        if (world == null) { return biomeDump.getLines(); }

        Registry<Biome> registry = world.getRegistryManager().get(RegistryKeys.BIOME);

        for (Identifier id : registry.getIds())
        {
            Biome biome = registry.get(id);
            String intId = String.valueOf(registry.getRawId(biome));
            String regName = id.toString();
            List<String> spawns = new ArrayList<>();

            for (SpawnGroup type : SpawnGroup.values())
            {
                List<String> tmpList = new ArrayList<>();

                // Add the spawns grouped by category and sorted alphabetically within each category
                for (SpawnSettings.SpawnEntry spawn : biome.getSpawnSettings().getSpawnEntries(type).getEntries())
                {
                    Identifier erl = Registries.ENTITY_TYPE.getId(spawn.type);
                    String entName = erl.toString();
                    int weight = spawn.getWeight().getValue();
                    tmpList.add(String.format("{ %s [weight: %d, min: %d, max: %d] }", entName, weight, spawn.minGroupSize, spawn.maxGroupSize));
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
        BlockPos pos = entity.getBlockPos();
        Biome biome = world.getBiome(pos).value();
        Registry<Biome> registry = world.getRegistryManager().get(RegistryKeys.BIOME);

        String intId = String.valueOf(registry.getRawId(biome));
        Formatting green = Formatting.GREEN;

        String regName = registry.getId(biome).toString();

        Biome.Precipitation rainType = biome.getPrecipitation(pos);
        BiomeEffects effects = biome.getEffects();
        int skyColor = effects.getSkyColor();
        int fogColor = effects.getFogColor();
        int waterColor = effects.getWaterColor();
        int waterFogColor = effects.getWaterFogColor();

        String strMaxSpawnChance = String.valueOf(biome.getSpawnSettings().getCreatureSpawnProbability());
        String strRainType = rainType.name();
        String strRainfall = "?"; //String.valueOf(biome.weather.downfall);
        String strTemperature = String.valueOf(biome.getTemperature());

        String strFogColor = String.format("0x%08X (%d)", fogColor, fogColor);
        String strSkyColor = String.format("0x%08X (%d)", skyColor, skyColor);
        String strWaterColor = String.format("0x%08X (%d)", waterColor, waterColor);
        String strWaterFogColor = String.format("0x%08X (%d)", waterFogColor, waterFogColor);

        boolean canSnow = biome.canSetSnow(world, pos);
        String strSnowing = canSnow ? "true" : "false";

        MutableText textPre = Text.literal("ID: ").append(Text.literal(intId).formatted(green))
                               .append(" - Registry name: ");

        entity.sendMessage(Text.literal("------------- Current biome info ------------"), false);
        entity.sendMessage(OutputUtils.getClipboardCopiableMessage(textPre, Text.literal(regName).formatted(green), Text.literal("")), false);

        entity.sendMessage(Text.literal("Temperature: ")
                                   .append(Text.literal(strTemperature).formatted(green)), false);
        entity.sendMessage(Text.literal("RainType: ").append(Text.literal(strRainType).formatted(green))
                                   .append(", downfall: ").append(Text.literal(strRainfall).formatted(green))
                                   .append(", snows: ").append(Text.literal(strSnowing).formatted(canSnow ? green : Formatting.RED)), false);
        entity.sendMessage(Text.literal("Max spawn chance: ").append(Text.literal(strMaxSpawnChance).formatted(green)), false);

        entity.sendMessage(Text.literal("Fog Color: ")
                                   .append(Text.literal(strFogColor).formatted(green)), false);
        entity.sendMessage(Text.literal("Sky Color: ")
                                   .append(Text.literal(strSkyColor).formatted(green)), false);
        entity.sendMessage(Text.literal("waterColorMultiplier: ")
                                   .append(Text.literal(strWaterColor).formatted(green)), false);
        entity.sendMessage(Text.literal("Water Fog Color: ")
                                   .append(Text.literal(strWaterFogColor).formatted(green)), false);

        entity.sendMessage(getMusicInfo(effects.getMusic()), false);
        entity.sendMessage(getAdditionsSoundInfo(effects.getAdditionsSound()), false);
        entity.sendMessage(getLoopSoundInfo(effects.getLoopSound()), false);
        entity.sendMessage(getMoodSoundInfo(effects.getMoodSound()), false);

        // Get the grass and foliage colors, if called on the client side
        TellMe.dataProvider.getCurrentBiomeInfoClientSide(entity, biome);
    }

    public static MutableText getMusicInfo(Optional<MusicSound> optional)
    {
        MutableText text = Text.literal("Music: ");
        Formatting green = Formatting.GREEN;

        if (optional.isPresent())
        {
            MusicSound sound = optional.get();
            Optional<RegistryKey<SoundEvent>> o = sound.getSound().getKey();

            if (o.isPresent())
            {
                Identifier id = o.get().getValue();
                String name = id != null ? id.toString() : "?";

                return text.append(Text.literal(name).formatted(green))
                               .append(", min delay: ").append(Text.literal(String.valueOf(sound.getMinDelay())).formatted(green))
                               .append(", max delay: ").append(Text.literal(String.valueOf(sound.getMaxDelay())).formatted(green))
                               .append(", replace current: ").append(Text.literal(String.valueOf(sound.shouldReplaceCurrentMusic())).formatted(green));
            }
        }

        return text.append(Text.literal("-").formatted(Formatting.RED));
    }

    public static MutableText getAdditionsSoundInfo(Optional<BiomeAdditionsSound> optional)
    {
        MutableText text = Text.literal("Additions Sound: ");
        Formatting green = Formatting.GREEN;

        if (optional.isPresent())
        {
            BiomeAdditionsSound sound = optional.get();
            Optional<RegistryKey<SoundEvent>> o = sound.getSound().getKey();

            if (o.isPresent())
            {
                Identifier id = o.get().getValue();
                String name = id != null ? id.toString() : "?";

                return text.append(Text.literal(name).formatted(green))
                               .append(", chance: ").append(Text.literal(String.valueOf(sound.getChance())).formatted(green));
            }
        }

        return text.append(Text.literal("-").formatted(Formatting.RED));
    }

    public static MutableText getLoopSoundInfo(Optional<RegistryEntry<SoundEvent>> optional)
    {
        MutableText text = Text.literal("Loop Sound: ");

        if (optional.isPresent() && optional.get().getKey().isPresent())
        {
            Identifier id = optional.get().getKey().get().getValue();
            String name = id != null ? id.toString() : "?";

            return text.append(Text.literal(name).formatted(Formatting.GREEN));
        }

        return text.append(Text.literal("-").formatted(Formatting.RED));
    }

    public static MutableText getMoodSoundInfo(Optional<BiomeMoodSound> optional)
    {
        MutableText text = Text.literal("Mood Sound: ");
        Formatting green = Formatting.GREEN;

        if (optional.isPresent())
        {
            BiomeMoodSound sound = optional.get();
            Optional<RegistryKey<SoundEvent>> o = sound.getSound().getKey();

            if (o.isPresent())
            {
                Identifier id = o.get().getValue();
                String name = id != null ? id.toString() : "?";

                return text.append(Text.literal(name).formatted(green))
                               .append(", delay: ").append(Text.literal(String.valueOf(sound.getCultivationTicks())).formatted(green))
                               .append(", range: ").append(Text.literal(String.valueOf(sound.getSpawnRange())).formatted(green))
                               .append(", extra distance: ").append(Text.literal(String.valueOf(sound.getExtraDistance())).formatted(green));
            }
        }

        return text.append(Text.literal("-").formatted(Formatting.RED));
    }

    public static List<String> getBiomeDumpIdToName(Format format, @Nullable World world)
    {
        List<IdToStringHolder> data = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        if (world == null) { return lines; }

        Registry<Biome> registry = world.getRegistryManager().get(RegistryKeys.BIOME);

        for (Identifier id : registry.getIds())
        {
            Biome biome = registry.get(id);
            int intId = registry.getRawId(biome);
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
            return 4;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("ID", "Registry name", "Temp.", "RainType @ 0,0");

            dump.setColumnProperties(2, Alignment.RIGHT, true); // temperature
            dump.setColumnProperties(3, Alignment.RIGHT, true); // raintype
        }

        @Override
        public void addLine(DataDump dump, Biome biome, Identifier id, BiomeDumpContext ctx)
        {
            if (ctx.world == null) { return; }

            Registry<Biome> registry = ctx.world.getRegistryManager().get(RegistryKeys.BIOME);
            String intId = String.valueOf(registry.getRawId(biome));
            String regName = id.toString();
            String temp = String.format("%5.2f", biome.getTemperature());
            Biome.Precipitation precipitation = biome.getPrecipitation(BlockPos.ORIGIN);
            String precStr = precipitation != Biome.Precipitation.NONE ? precipitation.name() : "-";

            dump.addData(intId, regName, temp, precStr);
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
        }

        @Override
        public void addLine(DataDump dump, Biome biome, Identifier id, BiomeDumpContext ctx)
        {
            if (ctx.world == null) { return; }

            Registry<Biome> registry = ctx.world.getRegistryManager().get(RegistryKeys.BIOME);
            String intId = String.valueOf(registry.getRawId(biome));
            String regName = id.toString();
            BiomeEffects effects = biome.getEffects();
            int skyColor = effects.getSkyColor();
            int fogColor = effects.getFogColor();
            int waterColor = effects.getWaterColor();
            int waterFogColor = effects.getWaterFogColor();
            String strFogColor = String.format("0x%08X (%d)", fogColor, fogColor);
            String strSkyColor = String.format("0x%08X (%d)", skyColor, skyColor);
            String strWaterColor = String.format("0x%08X (%d)", waterColor, waterColor);
            String strWaterFogColor = String.format("0x%08X (%d)", waterFogColor, waterFogColor);

            if (TellMe.isClient())
            {
                int foliageColor = biome.getFoliageColor();
                int grassColor = biome.getGrassColorAt(0, 0);
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

    public static class BiomeInfoProviderTags extends BiomeInfoProviderBase
    {
        @Override
        public int getColumnCount()
        {
            return 3;
        }

        @Override
        public void addTitle(DataDump dump)
        {
            dump.addTitle("ID", "Registry name", "Tags");
        }

        @Override
        public void addLine(DataDump dump, Biome biome, Identifier id, BiomeDumpContext ctx)
        {
            if (ctx.world == null) { return; }

            Registry<Biome> registry = ctx.world.getRegistryManager().get(RegistryKeys.BIOME);
            String intId = String.valueOf(registry.getRawId(biome));
            String regName = id.toString();
            String tags = registry.getEntry(biome).streamTags().map(e -> e.id().toString()).collect(Collectors.joining(", "));

            dump.addData(intId, regName, tags);
        }
    }
}
