package fi.dy.masa.tellme.command.argument;

import java.util.Collection;
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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class BiomeArgument implements ArgumentType<Identifier>
{
    private static final Collection<String> EXAMPLES = Stream.of(BiomeKeys.PLAINS, BiomeKeys.OCEAN).map((regKey) -> regKey.getValue().toString()).collect(Collectors.toList());

    public static final DynamicCommandExceptionType INVALID_BIOME_EXCEPTION = new DynamicCommandExceptionType((val) -> Text.literal("Invalid biome name: \"" + val + "\""));

    public Identifier parse(StringReader reader) throws CommandSyntaxException
    {
        return Identifier.fromCommandInput(reader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder)
    {
        return CommandSource.suggestIdentifiers(((CommandSource) ctx.getSource()).getRegistryManager().get(Registry.BIOME_KEY).getIds(), builder);
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

    public static Biome getBiomeArgument(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException
    {
        Identifier id = ctx.getArgument(name, Identifier.class);
        Biome biome = ctx.getSource().getRegistryManager().get(Registry.BIOME_KEY).get(id);

        if (biome == null)
        {
            throw INVALID_BIOME_EXCEPTION.create(id);
        }

        return biome;
    }
}
