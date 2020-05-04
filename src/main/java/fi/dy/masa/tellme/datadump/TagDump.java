package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class TagDump
{
    public static List<String> getFormattedTagDump(DataDump.Format format, TagType type, boolean split)
    {
        DataDump dump = new DataDump(2, format);

        switch (type)
        {
            case BLOCK:
            {
                Map<ResourceLocation, Tag<Block>> tagMap = BlockTags.getCollection().getTagMap();

                for (Map.Entry<ResourceLocation, Tag<Block>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getAllElements().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case ITEM:
            {
                Map<ResourceLocation, Tag<Item>> tagMap = ItemTags.getCollection().getTagMap();

                for (Map.Entry<ResourceLocation, Tag<Item>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getAllElements().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case FLUID:
            {
                Map<ResourceLocation, Tag<Fluid>> tagMap = FluidTags.getCollection().getTagMap();

                for (Map.Entry<ResourceLocation, Tag<Fluid>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getAllElements().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case ENTITY_TYPE:
            {
                Map<ResourceLocation, Tag<EntityType<?>>> tagMap = EntityTypeTags.getCollection().getTagMap();

                for (Map.Entry<ResourceLocation, Tag<EntityType<?>>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getAllElements().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }
        }

        dump.addTitle("ID", "Tagged objects");

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
