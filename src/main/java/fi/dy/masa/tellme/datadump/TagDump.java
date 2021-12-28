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
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.tellme.util.datadump.DataDump;

public class TagDump
{
    public static List<String> getFormattedTagDump(DataDump.Format format, TagType type, boolean split)
    {
        DataDump dump = new DataDump(2, format);

        switch (type)
        {
            case BLOCK:
            {
                Map<ResourceLocation, ITag<Block>> tagMap = BlockTags.getAllTags().getAllTags();

                for (Map.Entry<ResourceLocation, ITag<Block>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getValues().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case ITEM:
            {
                Map<ResourceLocation, ITag<Item>> tagMap = ItemTags.getAllTags().getAllTags();

                for (Map.Entry<ResourceLocation, ITag<Item>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getValues().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case FLUID:
            {
                Map<ResourceLocation, ITag<Fluid>> tagMap = FluidTags.getAllTags().getAllTags();

                for (Map.Entry<ResourceLocation, ITag<Fluid>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getValues().stream().map((b) -> b.getRegistryName().toString()), split);
                }

                break;
            }

            case ENTITY_TYPE:
            {
                Map<ResourceLocation, ITag<EntityType<?>>> tagMap = EntityTypeTags.getAllTags().getAllTags();

                for (Map.Entry<ResourceLocation, ITag<EntityType<?>>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().getValues().stream().map((b) -> b.getRegistryName().toString()), split);
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
