package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

@Mixin(DimensionType.class)
public interface IMixinDimensionType
{
    @Accessor
    Class <? extends WorldProvider> getClazz();
}
