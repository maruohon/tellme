package fi.dy.masa.tellme.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fi.dy.masa.tellme.command.CommandUtils;

public class GenericListArgument<T> implements ArgumentType<List<T>>
{
    protected final Function<String, T> stringToTypeFactory;
    protected final Function<T, String> typeToStringFactory;
    protected final Supplier<Collection<T>> suggestionsSupplier;
    protected final String emptyTypesMessage;

    protected GenericListArgument(Function<String, T> stringToTypeFactory, Function<T, String> typeToStringFactory,
            Supplier<Collection<T>> suggestionsSupplier, String emptyTypesMessage)
    {
        this.stringToTypeFactory = stringToTypeFactory;
        this.typeToStringFactory = typeToStringFactory;
        this.suggestionsSupplier = suggestionsSupplier;
        this.emptyTypesMessage = emptyTypesMessage;
    }

    public static <T> GenericListArgument<T> create(Function<String, T> stringToTypeFactory, Function<T, String> typeToStringFactory,
            Supplier<Collection<T>> suggestionsSupplier, String emptyTypesMessage)
    {
        return new GenericListArgument<T>(stringToTypeFactory, typeToStringFactory, suggestionsSupplier, emptyTypesMessage);
    }

    @Override
    public List<T> parse(StringReader reader) throws CommandSyntaxException
    {
        String[] parts = reader.getRemaining().split("\\s+");
        List<String> types =  Arrays.asList(parts);
        reader.setCursor(reader.getTotalLength());

        if (types.isEmpty())
        {
            CommandUtils.throwException(this.emptyTypesMessage);
        }

        return types.stream().map(this.stringToTypeFactory).collect(Collectors.toList());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return CommandUtils.suggestIterable(this.suggestionsSupplier.get()
                .stream().map(this.typeToStringFactory).collect(Collectors.toList()), builder);
    }
}
