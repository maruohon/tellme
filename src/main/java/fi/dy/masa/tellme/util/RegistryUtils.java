package fi.dy.masa.tellme.util;

import java.lang.invoke.MethodHandle;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.util.MethodHandleUtils.UnableToFindMethodHandleException;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryUtils
{
    private static MethodHandle methodHandle_ForgeRegistry_isDummied;

    static
    {
        try
        {
            methodHandle_ForgeRegistry_isDummied = MethodHandleUtils.getMethodHandleVirtual(
                    ForgeRegistry.class, new String[] { "isDummied" }, ResourceLocation.class);
        }
        catch (UnableToFindMethodHandleException e)
        {
            TellMe.logger.error("DataDump: Failed to get MethodHandle for ForgeRegistry#isDummied()", e);
        }
    }

    public static <K extends IForgeRegistryEntry<K>> boolean isDummied(IForgeRegistry<K> registry, ResourceLocation rl)
    {
        try
        {
            return (boolean) methodHandle_ForgeRegistry_isDummied.invoke(registry, rl);
        }
        catch (Throwable t)
        {
            TellMe.logger.error("DataDump: Error while trying invoke ForgeRegistry#isDummied()", t);
            return false;
        }
    }
}
