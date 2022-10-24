package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.Block;

@Mixin(Block.class)
public interface IMixinBlock
{
    @Accessor
    float getBlockHardness();

    @Accessor
    float getBlockResistance();
}
