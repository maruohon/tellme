package fi.dy.masa.tellme.util;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.datadump.DataDump;
import fi.dy.masa.tellme.util.datadump.DataDump.Format;

public class BiomeLocator
{
    private final Object2ObjectOpenHashMap<Biome, BlockPos> biomePositions = new Object2ObjectOpenHashMap<>();
    private final BlockPos.MutableBlockPos posMutable = new BlockPos.MutableBlockPos();
    private final Registry<Biome> registry;
    private BlockPos center = BlockPos.ZERO;
    private int count;
    private boolean append;

    public BiomeLocator(Registry<Biome> registry)
    {
        this.registry = registry;
    }

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    public void findClosestBiomePositions(BiomeManager biomeManager, BlockPos center, int sampleInterval, int maxRadius)
    {
        final long timeBefore = System.nanoTime();
        final int totalBiomes = this.registry.keySet().size();
        this.count = 0;
        this.center = center;

        if (this.append == false)
        {
            this.biomePositions.clear();
        }

        for (int radius = 0; radius <= maxRadius; radius++)
        {
            final int offset = radius * sampleInterval;

            if (this.samplePositionsOnRing(center.getX(), center.getZ(), offset, sampleInterval, totalBiomes, biomeManager))
            {
                break;
            }
        }

        final long timeAfter = System.nanoTime();
        TellMe.logger.info(String.format(Locale.US, "Sampled the biome in %d xz-locations in %.3f seconds",
                this.count, (double) (timeAfter - timeBefore) / 1000000000D));
    }

    private boolean samplePositionsOnRing(int centerX, int centerZ, int offset, int sampleInterval,
            int totalBiomes, BiomeManager biomeManager)
    {
        final int minX = centerX - offset;
        final int minZ = centerZ - offset;
        final int maxX = centerX + offset;
        final int maxZ = centerZ + offset;

        for (int x = minX; x <= maxX; x += sampleInterval)
        {
            if (this.samplePosition(x, minZ, totalBiomes, biomeManager))
            {
                return true;
            }
        }

        for (int z = minZ + sampleInterval; z <= maxZ; z += sampleInterval)
        {
            if (this.samplePosition(maxZ, z, totalBiomes, biomeManager))
            {
                return true;
            }
        }

        for (int x = maxX - sampleInterval; x >= minX; x -= sampleInterval)
        {
            if (this.samplePosition(x, maxZ, totalBiomes, biomeManager))
            {
                return true;
            }
        }

        for (int z = maxZ - sampleInterval; z > minZ; z -= sampleInterval)
        {
            if (this.samplePosition(minX, z, totalBiomes, biomeManager))
            {
                return true;
            }
        }

        return false;
    }

    private boolean samplePosition(int x, int z, int totalBiomes, BiomeManager biomeManager)
    {
        this.posMutable.set(x, 0, z);
        Biome biome = biomeManager.getBiome(this.posMutable).value();
        this.count++;

        BlockPos oldPos = this.biomePositions.get(biome);

        if (oldPos == null || oldPos.distSqr(this.center) > this.posMutable.distSqr(this.center))
        {
            this.biomePositions.put(biome, this.posMutable.immutable());

            return this.biomePositions.size() >= totalBiomes;
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
                TellMe.logger.warn("Null biome '{}' with position {} ?!", biome, entry.getValue());
                continue;
            }

            ResourceLocation key = this.registry.getKey(biome);

            dump.addData(
                    key != null ? key.toString() : "<null>",
                    String.valueOf(entry.getValue().getX()), String.valueOf(entry.getValue().getZ()));
        }

        dump.addTitle("Registry name", "X", "Z");
        dump.addHeader(String.format("Closest found biome locations around the center point x = %d, z = %d", this.center.getX(), this.center.getZ()));

        dump.setColumnProperties(1, DataDump.Alignment.RIGHT, true);
        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true);

        return dump.getLines();
    }
}
