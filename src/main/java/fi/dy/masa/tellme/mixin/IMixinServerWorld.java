package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

@Mixin(ServerWorld.class)
public interface IMixinServerWorld
{
    @Accessor("entitiesById")
    Int2ObjectMap<Entity> tellmeGetEntitiesByIdMap();
}
