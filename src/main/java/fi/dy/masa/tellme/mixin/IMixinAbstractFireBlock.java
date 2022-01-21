package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;

@Mixin(AbstractFireBlock.class)
public interface IMixinAbstractFireBlock
{
    @Invoker("isFlammable")
    boolean tellme_getIsFlammable(BlockState state);
}
