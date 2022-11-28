package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.dy.masa.tellme.util.datadump.DataDump;

public class TagDump
{
    public static List<String> getFormattedTagDump(DataDump.Format format, TagType type, boolean split)
    {
        DataDump dump = new DataDump(2, format);

        /*
        switch (type)
        {
            case BLOCK:
            {
                Map<ResourceLocation, Tag<Block>> tagMap = BlockTags.getAllTags().getAllTags();

                for (Map.Entry<ResourceLocation, Tag<Block>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getValues().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case ITEM:
            {
                Map<ResourceLocation, Tag<Item>> tagMap = ItemTags.getAllTags().getAllTags();

                for (Map.Entry<ResourceLocation, Tag<Item>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getValues().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case FLUID:
            {
                Map<ResourceLocation, Tag<Fluid>> tagMap = FluidTags.getAllTags().getAllTags();

                for (Map.Entry<ResourceLocation, Tag<Fluid>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getValues().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case ENTITY_TYPE:
            {
                Map<ResourceLocation, Tag<EntityType<?>>> tagMap = EntityTypeTags.getAllTags().getAllTags();

                for (Map.Entry<ResourceLocation, Tag<EntityType<?>>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getValues().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }
        }
        */

        dump.addTitle("ID", "Tagged objects");
        dump.addHeader("??? TODO 1.18.2+");

        return dump.getLines();
    }

    private static void addLines(DataDump dump, String tagId, Stream<String> stream, boolean split)
    {
        if (split)
        {
            stream.forEach((name) -> dump.addData(tagId, name));
        }
        else
        {
            dump.addData(tagId, stream.collect(Collectors.joining(", ")));
        }
    }

    public enum TagType
    {
        BLOCK,
        ITEM,
        FLUID,
        ENTITY_TYPE;
    }
}
