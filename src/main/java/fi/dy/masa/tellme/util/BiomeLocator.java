package fi.dy.masa.tellme.util;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import fi.dy.masa.tellme.LiteModTellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;

public class BiomeLocator
{
    private final Object2ObjectOpenHashMap<Biome, BlockPos> biomePositions = new Object2ObjectOpenHashMap<>();
    private BlockPos center = BlockPos.ORIGIN;
    private int count;
    private boolean append;

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    public void findClosestBiomePositions(BiomeProvider biomeProvider, BlockPos center, int sampleInterval, int maxRadius)
    {
        final long timeBefore = System.currentTimeMillis();
        final int totalBiomes = Biome.REGISTRY.getKeys().size();
        Biome[] biomes = new Biome[1];
        this.count = 0;
        this.center = center;

        if (this.append == false)
        {
            this.biomePositions.clear();
        }

        for (int radius = 0; radius <= maxRadius; radius++)
        {
            final int offset = radius * sampleInterval;

            if (this.samplePositionsOnRing(center.getX(), center.getZ(), offset, sampleInterval, totalBiomes, biomeProvider, biomes))
            {
                break;
            }
        }

        final long timeAfter = System.currentTimeMillis();
        LiteModTellMe.logger.info(String.format(Locale.US, "Sampled the biome in %d xz-locations in %.3f seconds",
                this.count, (timeAfter - timeBefore) / 1000f));
    }

    private boolean samplePositionsOnRing(int centerX, int centerZ, int offset, int sampleInterval,
            int totalBiomes, BiomeProvider biomeProvider, Biome[] biomes)
    {
        final int minX = centerX - offset;
        final int minZ = centerZ - offset;
        final int maxX = centerX + offset;
        final int maxZ = centerZ + offset;

        for (int x = minX; x <= maxX; x += sampleInterval)
        {
            if (this.samplePosition(x, minZ, totalBiomes, biomeProvider, biomes))
            {
                return true;
            }
        }

        for (int z = minZ + sampleInterval; z <= maxZ; z += sampleInterval)
        {
            if (this.samplePosition(maxZ, z, totalBiomes, biomeProvider, biomes))
            {
                return true;
            }
        }

        for (int x = maxX - sampleInterval; x >= minX; x -= sampleInterval)
        {
            if (this.samplePosition(x, maxZ, totalBiomes, biomeProvider, biomes))
            {
                return true;
            }
        }

        for (int z = maxZ - sampleInterval; z > minZ; z -= sampleInterval)
        {
            if (this.samplePosition(minX, z, totalBiomes, biomeProvider, biomes))
            {
                return true;
            }
        }

        return false;
    }

    private boolean samplePosition(int x, int z, int totalBiomes, BiomeProvider biomeProvider, Biome[] biomes)
    {
        biomeProvider.getBiomes(biomes, x, z, 1, 1, false);
        this.count++;

        if (this.biomePositions.containsKey(biomes[0]) == false)
        {
            this.biomePositions.put(biomes[0], new BlockPos(x, 0, z));

            if (this.biomePositions.size() >= totalBiomes)
            {
                return true;
            }
        }

        return false;
    }

    public List<String> getClosestBiomePositions(Format format)
    {
        DataDump dump = new DataDump(3, format);

        for (Map.Entry<Biome, BlockPos> entry : this.biomePositions.entrySet())
        {
            Biome biome = entry.getKey();

            if (biome == null)
            {
                LiteModTellMe.logger.warn("Null biome '{}' with position {} ?!", biome, entry.getValue());
                continue;
            }

            ResourceLocation key = Biome.REGISTRY.getNameForObject(biome);

            dump.addData(
                    key != null ? key.toString() : "<null>",
                    biome.getBiomeName(),
                    String.format("x = %5d, z = %5d", entry.getValue().getX(), entry.getValue().getZ()));
        }

        dump.addTitle("Registry name", "Name", "Closest location");
        dump.addHeader(String.format("Closest found biome locations around the center point x = %d, z = %d", this.center.getX(), this.center.getZ()));

        dump.setUseColumnSeparator(true);

        return dump.getLines();
    }
}
