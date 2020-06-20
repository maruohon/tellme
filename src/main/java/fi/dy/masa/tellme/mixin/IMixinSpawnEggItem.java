package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.item.SpawnEggItem;

@Mixin(SpawnEggItem.class)
public interface IMixinSpawnEggItem
{
    @Accessor("primaryColor")
    int getPrimaryColor();

    @Accessor("secondaryColor")
    int getSecondaryColor();
}
