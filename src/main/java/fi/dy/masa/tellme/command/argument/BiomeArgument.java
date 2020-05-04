package fi.dy.masa.tellme.command.argument;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeArgument implements ArgumentType<Biome>
{
    private static final Collection<String> EXAMPLES = Stream.of(Biomes.PLAINS, Biomes.OCEAN).map((biome) -> {
        return ForgeRegistries.BIOMES.getKey(biome).toString();
    }).collect(Collectors.toList());

    public static final DynamicCommandExceptionType INVALID_BIOME_EXCEPTION = new DynamicCommandExceptionType((val) -> {
        return new StringTextComponent("Invalid biome name: \"" + val + "\"");
    });

    public Biome parse(StringReader reader) throws CommandSyntaxException
    {
        ResourceLocation id = ResourceLocation.read(reader);
        Biome biome = ForgeRegistries.BIOMES.getValue(id);

        if (biome != null)
        {
            return biome;
        }

        throw INVALID_BIOME_EXCEPTION.create(id);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
    {
        return ISuggestionProvider.func_212476_a(Streams.stream(ForgeRegistries.BIOMES).map(Biome::getRegistryName), builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    public static BiomeArgument create()
    {
        return new BiomeArgument();
    }

    public static Biome getBiomeArgument(CommandContext<CommandSource> ctx, String name)
    {
        return ctx.getArgument(name, Biome.class);
    }
}
