package fi.dy.masa.tellme.command.argument;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fi.dy.masa.tellme.command.CommandUtils;

public class StringCollectionArgument implements ArgumentType<List<String>>
{
    protected final Supplier<Collection<String>> suggestionsSupplier;
    protected final String emptyTypesMessage;

    protected StringCollectionArgument(Supplier<Collection<String>> suggestionsSupplier, String emptyTypesMessage)
    {
        this.suggestionsSupplier = suggestionsSupplier;
        this.emptyTypesMessage = emptyTypesMessage;
    }

    public static StringCollectionArgument create(Supplier<Collection<String>> suggestionsSupplier, String emptyTypesMessage)
    {
        return new StringCollectionArgument(suggestionsSupplier, emptyTypesMessage);
    }

    @Override
    public List<String> parse(StringReader reader) throws CommandSyntaxException
    {
        String[] parts = reader.getRemaining().split("\\s+");
        List<String> types =  Arrays.asList(parts);
        reader.setCursor(reader.getTotalLength());

        if (types.isEmpty())
        {
            CommandUtils.throwException(this.emptyTypesMessage);
        }

        return types;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return CommandUtils.suggestIterable(this.suggestionsSupplier.get(), builder);
    }
}
