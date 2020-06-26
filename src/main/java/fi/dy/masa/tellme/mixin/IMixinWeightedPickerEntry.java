package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.util.collection.WeightedPicker;

@Mixin(WeightedPicker.Entry.class)
public interface IMixinWeightedPickerEntry
{
    @Accessor("weight")
    int getWeight();
}
