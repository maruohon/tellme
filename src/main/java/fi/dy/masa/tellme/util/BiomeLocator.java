package fi.dy.masa.tellme.util;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.datadump.DataDump;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeLocator
{
    private final Object2ObjectOpenHashMap<Biome, BlockPos> biomePositions = new Object2ObjectOpenHashMap<>();
    private BlockPos center = BlockPos.ZERO;
    private int count;
    private boolean append;

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    public void findClosestBiomePositions(BiomeProvider biomeProvider, BlockPos center, int sampleInterval, int maxRadius)
    {
        final long timeBefore = System.nanoTime();
        final int totalBiomes = ForgeRegistries.BIOMES.getKeys().size();
        this.count = 0;
        this.center = center;

        if (this.append == false)
        {
            this.biomePositions.clear();
        }

        for (int radius = 0; radius <= maxRadius; radius++)
        {
            final int offset = radius * sampleInterval;

            if (this.samplePositionsOnRing(center.getX(), center.getZ(), offset, sampleInterval, totalBiomes, biomeProvider))
            {
                break;
            }
        }

        final long timeAfter = System.nanoTime();
        TellMe.logger.info(String.format(Locale.US, "Sampled the biome in %d xz-locations in %.3f seconds",
                this.count, (double) (timeAfter - timeBefore) / 1000000000D));
    }

    private boolean samplePositionsOnRing(int centerX, int centerZ, int offset, int sampleInterval,
            int totalBiomes, BiomeProvider biomeProvider)
    {
        final int minX = centerX - offset;
        final int minZ = centerZ - offset;
        final int maxX = centerX + offset;
        final int maxZ = centerZ + offset;

        for (int x = minX; x <= maxX; x += sampleInterval)
        {
            if (this.samplePosition(x, minZ, totalBiomes, biomeProvider))
            {
                return true;
            }
        }

        for (int z = minZ + sampleInterval; z <= maxZ; z += sampleInterval)
        {
            if (this.samplePosition(maxZ, z, totalBiomes, biomeProvider))
            {
                return true;
            }
        }

        for (int x = maxX - sampleInterval; x >= minX; x -= sampleInterval)
        {
            if (this.samplePosition(x, maxZ, totalBiomes, biomeProvider))
            {
                return true;
            }
        }

        for (int z = maxZ - sampleInterval; z > minZ; z -= sampleInterval)
        {
            if (this.samplePosition(minX, z, totalBiomes, biomeProvider))
            {
                return true;
            }
        }

        return false;
    }

    private boolean samplePosition(int x, int z, int totalBiomes, BiomeProvider biomeProvider)
    {
        Biome[] biomes = biomeProvider.getBiomes(x, z, 1, 1, false);
        this.count++;

        BlockPos newPos = new BlockPos(x, 0, z);
        BlockPos oldPos = this.biomePositions.get(biomes[0]);

        if (oldPos == null || oldPos.distanceSq(this.center) > newPos.distanceSq(this.center))
        {
            this.biomePositions.put(biomes[0], newPos);

            if (this.biomePositions.size() >= totalBiomes)
            {
                return true;
            }
        }

        return false;
    }

    public List<String> getClosestBiomePositions(Format format)
    {
        DataDump dump = new DataDump(4, format);

        for (Map.Entry<Biome, BlockPos> entry : this.biomePositions.entrySet())
        {
            Biome biome = entry.getKey();

            if (biome == null)
            {
                TellMe.logger.warn("Null biome '{}' with position {} ?!", biome, entry.getValue());
                continue;
            }

            ResourceLocation key = ForgeRegistries.BIOMES.getKey(biome);

            dump.addData(
                    key != null ? key.toString() : "<null>",
                    getBiomeDisplayName(biome),
                    String.valueOf(entry.getValue().getX()), String.valueOf(entry.getValue().getZ()));
        }

        dump.addTitle("Registry name", "Name", "X", "Z");
        dump.addHeader(String.format("Closest found biome locations around the center point x = %d, z = %d", this.center.getX(), this.center.getZ()));

        dump.setColumnProperties(2, DataDump.Alignment.RIGHT, true);
        dump.setColumnProperties(3, DataDump.Alignment.RIGHT, true);

        return dump.getLines();
    }

    public static String getBiomeDisplayName(Biome biome)
    {
        return (new TranslationTextComponent(biome.getTranslationKey())).getString();
    }
}
