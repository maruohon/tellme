package fi.dy.masa.tellme.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryUtils
{
    public static <T> ResourceLocation getId(T obj, IForgeRegistry<T> registry)
    {
        return registry.getKey(obj);
    }

    public static <T> String getIdStr(T obj, IForgeRegistry<T> registry)
    {
        ResourceLocation id = registry.getKey(obj);
        return id != null ? id.toString() : "<null>";
    }
}
