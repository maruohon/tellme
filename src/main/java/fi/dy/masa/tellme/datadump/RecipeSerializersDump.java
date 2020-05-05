package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.datadump.DataDump.Format;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeSerializersDump
{
    public static List<String> getFormattedDump(Format format)
    {
        DataDump dump = new DataDump(1, format);

        for (Map.Entry<ResourceLocation, IRecipeSerializer<?>> entry : ForgeRegistries.RECIPE_SERIALIZERS.getEntries())
        {
            IRecipeSerializer<?> type = entry.getValue();
            dump.addData(type.getRegistryName().toString());
        }

        dump.addTitle("Registry name");

        return dump.getLines();
    }
}
