package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.SoundEvent;

@Mixin(BiomeAdditionsSound.class)
public interface IMixinBiomeAdditionsSound
{
    @Accessor("sound")
    SoundEvent tellmeGetSound();

    @Accessor("chance")
    double tellmeGetPlayChance();
}
