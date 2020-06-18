package fi.dy.masa.tellme.command.argument;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.LiteralText;
import fi.dy.masa.tellme.command.CommandUtils;
import fi.dy.masa.tellme.command.CommandUtils.OutputType;

public class OutputTypeArgument implements ArgumentType<OutputType>
{
    private static final ImmutableList<String> TYPE_ARGUMENTS = ImmutableList.copyOf(Arrays.asList(OutputType.values()).stream().map((type) -> type.getArgument()).collect(Collectors.toList()));

    public static final DynamicCommandExceptionType NO_SUCH_TYPE_EXCEPTION = new DynamicCommandExceptionType((v) -> new LiteralText("Unknown output type: " + v));
    private static final SimpleCommandExceptionType EMPTY_TYPE = new SimpleCommandExceptionType(new LiteralText("No output type given"));

    public static OutputTypeArgument create()
    {
        return new OutputTypeArgument();
    }

    @Override
    public OutputType parse(StringReader reader) throws CommandSyntaxException
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

        return OutputType.fromArg(type);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        return CommandUtils.suggestIterable(TYPE_ARGUMENTS, builder);
    }
}
