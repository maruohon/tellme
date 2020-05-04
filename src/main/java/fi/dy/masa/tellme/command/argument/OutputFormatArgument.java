package fi.dy.masa.tellme.command.argument;

import java.util.concurrent.CompletableFuture;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.util.text.StringTextComponent;
import fi.dy.masa.tellme.command.CommandUtils;
import fi.dy.masa.tellme.datadump.DataDump;

public class OutputFormatArgument implements ArgumentType<DataDump.Format>
{
    public static final DynamicCommandExceptionType NO_SUCH_TYPE_EXCEPTION = new DynamicCommandExceptionType((v) -> {
        return new StringTextComponent("Unknown output format: " + v);
    });
    private static final SimpleCommandExceptionType EMPTY_TYPE = new SimpleCommandExceptionType(new StringTextComponent("No output format given"));

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
        return CommandUtils.suggestIterable(ImmutableList.of("ascii", "csv"), builder);
    }
}
