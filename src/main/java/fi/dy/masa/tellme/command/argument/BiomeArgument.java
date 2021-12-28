package fi.dy.masa.tellme.command.argument;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class BiomeArgument implements ArgumentType<ResourceLocation>
{
    private static final Collection<String> EXAMPLES = Stream.of(Biomes.PLAINS, Biomes.OCEAN).map((regKey) -> regKey.location().toString()).collect(Collectors.toList());

    public static final DynamicCommandExceptionType INVALID_BIOME_EXCEPTION = new DynamicCommandExceptionType((val) -> new StringTextComponent("Invalid biome name: \"" + val + "\""));

    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException
    {
        return ResourceLocation.read(reader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
    {
        return ISuggestionProvider.suggestResource(((CommandSource) ctx.getSource()).registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).keySet(), builder);
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

    public static Biome getBiomeArgument(CommandContext<CommandSource> ctx, String name) throws CommandSyntaxException
    {
        ResourceLocation id = ctx.getArgument(name, ResourceLocation.class);
        Optional<Biome> optional = ctx.getSource().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOptional(id);

        if (optional.isPresent() == false)
        {
            throw INVALID_BIOME_EXCEPTION.create(id);
        }

        return optional.get();
    }
}
