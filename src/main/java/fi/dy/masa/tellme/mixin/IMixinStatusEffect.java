package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

@Mixin(StatusEffect.class)
public interface IMixinStatusEffect
{
    @Accessor("type")
    StatusEffectType tellmeGetEffectType();
}
