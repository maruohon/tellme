package fi.dy.masa.tellme.datadump;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.dy.masa.tellme.util.datadump.DataDump;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

public class TagDump
{
    @SuppressWarnings("unchecked")
    public static List<String> getFormattedTagDump(DataDump.Format format, TagType type, boolean split)
    {
        DataDump dump = new DataDump(2, format);

        switch (type)
        {
            case BLOCK:
            {
                ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
                
                for (Object oTag : tags.getTagNames().toArray()) {
                    TagKey<Block> tagKey = (TagKey<Block>)oTag;
                    ITag<Block> iTag = tags.getTag(tagKey);
                    Stream<String> elementString = iTag.stream().map(TagDump::getBlockRegistryString);

                    addLines(dump, tagKey.location().toString(),
                             elementString, 
                             split);                    
                }           

                break;
            }

            case ITEM:
            {
                ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
                
                for (Object oTag : tags.getTagNames().toArray()) {
                    TagKey<Item> tagKey = (TagKey<Item>)oTag;
                    ITag<Item> iTag = tags.getTag(tagKey);
                    Stream<String> elementString = iTag.stream().map(TagDump::getItemRegistryString);

                    addLines(dump, tagKey.location().toString(),
                             elementString, 
                             split);                    
                }           

                break;
            }

            case FLUID:
            {
                ITagManager<Fluid> tags = ForgeRegistries.FLUIDS.tags();
                
                for (Object oTag : tags.getTagNames().toArray()) {
                    TagKey<Fluid> tagKey = (TagKey<Fluid>)oTag;
                    ITag<Fluid> iTag = tags.getTag(tagKey);
                    Stream<String> elementString = iTag.stream().map(TagDump::getFluidRegistryString);

                    addLines(dump, tagKey.location().toString(),
                             elementString, 
                             split);                    
                }           

                break;
            }

            case ENTITY_TYPE:
            {
                ITagManager<EntityType<?>> tags = ForgeRegistries.ENTITY_TYPES.tags();
                
                for (Object oTag : tags.getTagNames().toArray()) {
                    TagKey<EntityType<?>> tagKey = (TagKey<EntityType<?>>)oTag;
                    ITag<EntityType<?>> iTag = tags.getTag(tagKey);
                    Stream<String> elementString = iTag.stream().map(TagDump::getEntityTypeRegistryString);

                    addLines(dump, tagKey.location().toString(),
                             elementString, 
                             split);                    
                }           

                break;
            }
        }

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

    // public static String getRegistryString(IForgeRegistry<T> registry, T i) {
    //     ResourceKey<T> k = registry.getResourceKey(i).get();
    //     ResourceLocation l = k.location();
    //     return l.toString();
    // }

    public static String getItemRegistryString(Item i) {
        ResourceKey<Item> k = ForgeRegistries.ITEMS.getResourceKey(i).get();
        ResourceLocation l = k.location();
        return l.toString();
    }

    public static String getBlockRegistryString(Block i) {
        ResourceKey<Block> k = ForgeRegistries.BLOCKS.getResourceKey(i).get();
        ResourceLocation l = k.location();
        return l.toString();
    }

    public static String getFluidRegistryString(Fluid i) {
        ResourceKey<Fluid> k = ForgeRegistries.FLUIDS.getResourceKey(i).get();
        ResourceLocation l = k.location();
        return l.toString();
    }

    public static String getEntityTypeRegistryString(EntityType<?> i) {
        ResourceKey<EntityType<?>> k = ForgeRegistries.ENTITY_TYPES.getResourceKey(i).get();
        ResourceLocation l = k.location();
        return l.toString();
    }


}
