package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.tag.FluidTags;

@Mixin(FluidTags.class)
public interface IMixinFluidTags
{
    /*
    @Accessor("REQUIRED_TAGS")
    static RequiredTagList<Fluid> tellme_getRequiredTags() { return null; }
    */
}
