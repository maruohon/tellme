package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;

@Mixin(MusicSound.class)
public interface IMixinMusicSound
{
    @Accessor("sound")
    SoundEvent tellmeGetSound();

    @Accessor("minDelay")
    int tellmeGetMinDelay();

    @Accessor("maxDelay")
    int tellmeGetMaxDelay();

    @Accessor("replaceCurrentMusic")
    boolean tellmeGetCanStop();
}
