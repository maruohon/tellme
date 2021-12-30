package fi.dy.masa.tellme.command.argument;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import fi.dy.masa.tellme.util.datadump.DataDump;

public class OutputFormatArgument implements ArgumentType<DataDump.Format>
{
    private static final SimpleCommandExceptionType EMPTY_TYPE = new SimpleCommandExceptionType(new TextComponent("No output format given"));
    private static final ImmutableList<String> SUGGESTIONS = ImmutableList.copyOf(Stream.of(DataDump.Format.values()).map((val) -> val.getArgument()).collect(Collectors.toList()));

    public static OutputFormatArgument create()
    {
        return new OutputFormatArgument();
    }

    @Override
    public DataDump.Format parse(StringReader reader) throws CommandSyntaxException
    {
        final int startPos = reader.getCursor();

        while (reader.canRead() && reader.peek() != ' ')
        {
            reader.skip();
        }

        String type = reader.getString().substring(startPos, reader.getCursor());

        if (type.isEmpty())
        {
            throw EMPTY_TYPE.create();
        }

        return DataDump.Format.fromArg(type);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return CommandUtils.suggestIterable(SUGGESTIONS, builder);
    }
}
