package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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
                Map<Identifier, Tag<Block>> tagMap = BlockTags.getContainer().getEntries();

                for (Map.Entry<Identifier, Tag<Block>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                             entry.getValue().values().stream().map((b) -> Registry.BLOCK.getId(b).toString()), split);
                }

                break;
            }

            case ITEM:
            {
                Map<Identifier, Tag<Item>> tagMap = ItemTags.getContainer().getEntries();

                for (Map.Entry<Identifier, Tag<Item>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().values().stream().map((i) -> Registry.ITEM.getId(i).toString()), split);
                }

                break;
            }

            case FLUID:
            {
                Map<Identifier, Tag<Fluid>> tagMap = FluidTags.getContainer().getEntries();

                for (Map.Entry<Identifier, Tag<Fluid>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().values().stream().map((f) -> Registry.FLUID.getId(f).toString()), split);
                }

                break;
            }

            case ENTITY_TYPE:
            {
                Map<Identifier, Tag<EntityType<?>>> tagMap = EntityTypeTags.getContainer().getEntries();

                for (Map.Entry<Identifier, Tag<EntityType<?>>> entry : tagMap.entrySet())
                {
                    addLines(dump, entry.getKey().toString(),
                            entry.getValue().values().stream().map((e) -> Registry.ENTITY_TYPE.getId(e).toString()), split);
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
