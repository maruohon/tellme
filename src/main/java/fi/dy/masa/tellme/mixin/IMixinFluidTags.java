package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.RequiredTagList;

@Mixin(FluidTags.class)
public interface IMixinFluidTags
{
    @Accessor("REQUIRED_TAGS")
    static RequiredTagList<Fluid> tellme_getRequiredTags() { return null; }
}
