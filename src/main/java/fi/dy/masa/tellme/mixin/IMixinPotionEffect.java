package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.potion.PotionEffect;

@Mixin(PotionEffect.class)
public interface IMixinPotionEffect
{
    @Accessor
    boolean getIsSplashPotion();
}
