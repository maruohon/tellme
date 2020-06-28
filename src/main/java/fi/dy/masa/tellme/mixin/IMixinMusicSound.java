package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;

@Mixin(MusicSound.class)
public interface IMixinMusicSound
{
    @Accessor("event")
    SoundEvent tellmeGetSound();

    @Accessor("field_24058")
    int tellmeGetMinDelay();

    @Accessor("field_24059")
    int tellmeGetMaxDelay();

    @Accessor("field_24060")
    boolean tellmeGetCanStop();
}
