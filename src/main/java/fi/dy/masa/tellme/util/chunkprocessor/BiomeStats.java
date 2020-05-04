package fi.dy.masa.tellme.util.chunkprocessor;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Alignment;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import fi.dy.masa.tellme.util.BiomeLocator;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeStats
{
    private final Object2LongOpenHashMap<Biome> biomeCounts = new Object2LongOpenHashMap<Biome>();
    private int totalCount;
    private boolean append;

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    public void getFullBiomeDistribution(BiomeProvider biomeProvider, BlockPos posMin, BlockPos posMax)
    {
        Object2LongOpenHashMap<Biome> counts = new Object2LongOpenHashMap<>();
        final long timeBefore = System.nanoTime();
        final int chunkMinX = posMin.getX() >> 4;
        final int chunkMinZ = posMin.getZ() >> 4;
        final int chunkMaxX = posMax.getX() >> 4;
        final int chunkMaxZ = posMax.getZ() >> 4;
        long count = 0;

        for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++)
        {
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++)
            {
                final int xMin = Math.max(chunkX << 4, posMin.getX());
                final int zMin = Math.max(chunkZ << 4, posMin.getZ());
                final int xMax = Math.min((chunkX << 4) + 15, posMax.getX());
                final int zMax = Math.min((chunkZ << 4) + 15, posMax.getZ());
                final int width = xMax - xMin + 1;
                final int length = zMax - zMin + 1;

                Biome[] biomes = biomeProvider.getBiomes(xMin, zMin, width, length, false);

                for (int x = 0; x < width; x++)
                {
                    for (int z = 0; z < length; z++)
                    {
                        counts.addTo(biomes[x * length + z], 1);
                    }
                }

                count += width * length;
            }
        }

        final long timeAfter = System.nanoTime();
        TellMe.logger.info(String.format(Locale.US, "Counted the biome for %d xz-locations in %.3f seconds", count, (timeAfter - timeBefore) / 1000000000D));

        this.addData(counts, count);
    }

    public void getSampledBiomeDistribution(BiomeProvider biomeProvider, int centerX, int centerZ, int sampleInterval, int sampleRadius)
    {
        Object2LongOpenHashMap<Biome> counts = new Object2LongOpenHashMap<>();
        final long timeBefore = System.nanoTime();
        final int endX = centerX + sampleRadius * sampleInterval;
        final int endZ = centerZ + sampleRadius * sampleInterval;
        long count = 0;

        for (int z = centerZ - sampleRadius * sampleInterval; z <= endZ; z += sampleInterval)
        {
            for (int x = centerX - sampleRadius * sampleInterval; x <= endX; x += sampleInterval)
            {
                Biome[] biomes = biomeProvider.getBiomes(x, z, 1, 1, false);
                counts.addTo(biomes[0], 1);
                ++count;
            }
        }

        final long timeAfter = System.nanoTime();
        TellMe.logger.info(String.format(Locale.US, "Counted the biome for %d xz-locations in %.3f seconds", count, (timeAfter - timeBefore) / 1000000000D));

        this.addData(counts, count);
    }

    private void addData(Object2LongOpenHashMap<Biome> counts, long count)
    {
        if (this.append == false)
        {
            this.biomeCounts.clear();
            this.totalCount = 0;
        }

        for (Map.Entry<Biome, Long> entry : counts.object2LongEntrySet())
        {
            if (this.append)
            {
                this.biomeCounts.addTo(entry.getKey(), entry.getValue());
            }
            else
            {
                this.biomeCounts.put(entry.getKey(), (long) entry.getValue());
            }
        }

        this.totalCount += count;
    }

    private void addFilteredData(DataDump dump, List<String> filters)
    {
        for (String filter : filters)
        {
            int firstSemi = filter.indexOf(":");

            if (firstSemi == -1)
            {
                filter = "minecraft:" + filter;
            }

            ResourceLocation key = null;
            Biome biome = null;

            try
            {
                key = new ResourceLocation(filter);
            }
            catch (Exception e)
            {
            }

            biome = key != null ? ForgeRegistries.BIOMES.getValue(key) : null;

            if (biome == null)
            {
                TellMe.logger.warn("Invalid biome name '{}'", filter);
                continue;
            }

            for (Map.Entry<Biome, Long> entry : this.biomeCounts.object2LongEntrySet())
            {
                if (entry.getKey() == biome)
                {
                    @SuppressWarnings("deprecation")
                    int id = Registry.BIOME.getId(biome);
                    long count = entry.getValue();

                    dump.addData(
                            key.toString(),
                            BiomeLocator.getBiomeDisplayName(biome),
                            String.valueOf(id),
                            String.valueOf(count),
                            String.format("%.2f %%", (double) count * 100D / (double) this.totalCount));
                    break;
                }
            }
        }
    }

    public List<String> queryAll(Format format)
    {
        return this.query(format, null);
    }

    public List<String> query(Format format, @Nullable List<String> filters)
    {
        DataDump dump = new DataDump(5, format);

        if (filters != null)
        {
            this.addFilteredData(dump, filters);
        }
        else
        {
            for (Map.Entry<Biome, Long> entry : this.biomeCounts.object2LongEntrySet())
            {
                Biome biome = entry.getKey();

                if (biome == null)
                {
                    TellMe.logger.warn("Null biome '{}' with count {} ?!", biome, entry.getValue());
                    continue;
                }

                ResourceLocation key = ForgeRegistries.BIOMES.getKey(biome);
                @SuppressWarnings("deprecation")
                int id = Registry.BIOME.getId(biome);
                long count = entry.getValue();

                dump.addData(
                        key != null ? key.toString() : "<null>",
                        BiomeLocator.getBiomeDisplayName(biome),
                        String.valueOf(id),
                        String.valueOf(count),
                        String.format("%.2f", (double) count * 100D / (double) this.totalCount));
            }
        }

        dump.addTitle("Registry name", "Name", "ID", "Count", "%");

        dump.setColumnProperties(2, Alignment.RIGHT, true); // Biome ID
        dump.setColumnProperties(3, Alignment.RIGHT, true); // count
        dump.setColumnProperties(4, Alignment.RIGHT, true); // count %

        return dump.getLines();
    }
}
