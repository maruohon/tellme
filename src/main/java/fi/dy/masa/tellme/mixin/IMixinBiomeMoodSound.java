package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.SoundEvent;

@Mixin(BiomeMoodSound.class)
public interface IMixinBiomeMoodSound
{
    @Accessor("event")
    SoundEvent tellmeGetSound();

    @Accessor("cultivationTicks")
    int tellmeGetCultivationTicks();

    @Accessor("spawnRange")
    int tellmeGetSpawnRange();

    @Accessor("extraDistance")
    double tellmeGetExtraDistance();
}
