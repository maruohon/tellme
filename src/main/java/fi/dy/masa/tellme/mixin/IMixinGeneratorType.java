package fi.dy.masa.tellme.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.world.GeneratorType;

@Mixin(GeneratorType.class)
public interface IMixinGeneratorType
{
    @Accessor("VALUES")
    static List<GeneratorType> tellme_getValues() { return null; }
}
