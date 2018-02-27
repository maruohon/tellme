package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

@Mixin(TileEntity.class)
public interface IMixinTileEntity
{
    @Accessor("REGISTRY")
    public static RegistryNamespaced<ResourceLocation, Class <? extends TileEntity>> getRegistry() { return null; }
}
