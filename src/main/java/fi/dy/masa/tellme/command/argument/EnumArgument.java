package fi.dy.masa.tellme.command.argument;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.TextComponent;
import fi.dy.masa.tellme.command.CommandUtils;

public class EnumArgument<T extends Enum<T>> implements ArgumentType<T>
{
    private static final SimpleCommandExceptionType EMPTY_TYPE = new SimpleCommandExceptionType(new TextComponent("No argument given"));

    private final Function<String, T> entryFactory;
    private final ImmutableList<String> typeArguments;

    protected EnumArgument(List<T> values, Function<String, T> stringToEntryFactory, Function<T, String> entryToStringFactory)
    {
        this.entryFactory = stringToEntryFactory;
        this.typeArguments = ImmutableList.copyOf(values.stream().map((entry) -> entryToStringFactory.apply(entry)).collect(Collectors.toList()));
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException
    {
        final int startPos = reader.getCursor();

        while (reader.canRead() && reader.peek() != ' ')
        {
            reader.skip();
        }

        String str = reader.getString().substring(startPos, reader.getCursor());

        if (str.isEmpty())
        {
            throw EMPTY_TYPE.create();
        }

        return this.entryFactory.apply(str);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return CommandUtils.suggestIterable(this.typeArguments, builder);
    }
}
