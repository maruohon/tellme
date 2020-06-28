package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.biome.Biome;

@Mixin(Biome.class)
public interface IMixinBiome
{
    @Accessor("skyColor")
    int tellmeGetSkyColor();
}
