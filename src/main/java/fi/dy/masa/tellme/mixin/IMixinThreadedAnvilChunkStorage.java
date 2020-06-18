package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface IMixinThreadedAnvilChunkStorage
{
    @Accessor("chunkHolders")
    Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkHolders();
}
