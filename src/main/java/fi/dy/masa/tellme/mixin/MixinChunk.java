package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.chunk.Chunk;
import fi.dy.masa.tellme.event.datalogging.EventHandlers;

@Mixin(Chunk.class)
public class MixinChunk
{
    @Inject(method = "onLoad", at = @At("RETURN"))
    private void hookOnLoad(CallbackInfo ci)
    {
        EventHandlers.onChunkLoad((Chunk) (Object) this);
    }

    @Inject(method = "onUnload", at = @At("RETURN"))
    private void hookOnUnload(CallbackInfo ci)
    {
        EventHandlers.onChunkUnload((Chunk) (Object) this);
    }
}
