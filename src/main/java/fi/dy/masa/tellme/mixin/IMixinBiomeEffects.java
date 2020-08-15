package fi.dy.masa.tellme.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.biome.BiomeEffects;

@Mixin(BiomeEffects.class)
public interface IMixinBiomeEffects
{
    @Accessor("fogColor")
    int tellmeGetFogColor();

    @Accessor("skyColor")
    int tellmeGetSkyColor();

    @Accessor("waterColor")
    int tellmeGetWaterColor();

    @Accessor("waterFogColor")
    int tellmeGetWaterFogColor();

    @Accessor("loopSound")
    Optional<SoundEvent> tellmeGetLoopSound();

    @Accessor("moodSound")
    Optional<BiomeMoodSound> tellmeGetMoodSound();

    @Accessor("additionsSound")
    Optional<BiomeAdditionsSound> tellmeGetAdditionsSound();

    @Accessor("music")
    Optional<MusicSound> tellmeGetMusic();
}
