package fi.dy.masa.tellme.datadump;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import fi.dy.masa.tellme.util.ModNameUtils;

public class DumpUtils
{
    public static <T extends IForgeRegistryEntry<T>>
    String getPackDevUtilsSnippetData(IForgeRegistry<T> registry, String dataTypeId)
    {
        HashMultimap<String, ResourceLocation> map = HashMultimap.create(128, 512);

        // Get a mapping of modName => collection-of-entry-names
        for (Map.Entry<ResourceKey<T>, T> entry : registry.getEntries())
        {
            ResourceLocation key = entry.getValue().getRegistryName();
            map.put(key.getNamespace(), key);
        }

        // First sort by mod name
        List<String> modIds = Lists.newArrayList(map.keySet());
        Collections.sort(modIds);
        JsonObject root = new JsonObject();
        String dataTypeName = dataTypeId.substring(0, 1).toUpperCase(Locale.ROOT) + dataTypeId.substring(1);

        /* example:
        "Apotheosis Blocks": {
            "body": "apotheosis:${1|beeshelf,blazing_hellshelf,boss_spawner,crystal_seashelf,draconic_endshelf,endshelf,glowing_hellshelf,heart_seashelf,hellshelf,melonshelf,pearl_endshelf,prismatic_altar,seashelf|}",
            "prefix": "@apotheosis/blocks/"
        },
        */

        for (String modId : modIds)
        {
            // For each mod, sort the entries by their registry name
            List<ResourceLocation> entryIds = Lists.newArrayList(map.get(modId));
            Collections.sort(entryIds);

            String idListString = entryIds.stream().map(ResourceLocation::getPath).collect(Collectors.joining(","));
            JsonObject obj = new JsonObject();
            obj.addProperty("body", String.format("%s:${1|%s|}", modId, idListString));
            obj.addProperty("prefix", String.format("@%s/%s/", modId, dataTypeId));

            String modName = ModNameUtils.getModName(modId);
            String dataKeyName = String.format("%s %s", modName, dataTypeName);
            root.add(dataKeyName, obj);
        }

        return (new GsonBuilder()).setPrettyPrinting().create().toJson(root);
    }
}
