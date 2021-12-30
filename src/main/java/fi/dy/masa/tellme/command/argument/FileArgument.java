package fi.dy.masa.tellme.command.argument;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.TextComponent;
import fi.dy.masa.tellme.command.CommandUtils;

public class FileArgument implements ArgumentType<File>
{
    public static final DynamicCommandExceptionType NO_SUCH_FILE_EXCEPTION = new DynamicCommandExceptionType((v) -> new TextComponent("No such file: " + v));
    private static final SimpleCommandExceptionType EMPTY_FILE_NAME = new SimpleCommandExceptionType(new TextComponent("Empty file name"));
    private static final SimpleCommandExceptionType NO_DIRECTORY = new SimpleCommandExceptionType(new TextComponent("No base directory set"));

    @Nullable private final File dir;
    private final boolean shouldExist;

    private FileArgument(@Nullable File dir, boolean shouldExist)
    {
        this.dir = dir;
        this.shouldExist = shouldExist;
    }

    public static FileArgument createEmpty()
    {
        return new FileArgument(null, false);
    }

    public static FileArgument getFor(File dir, boolean shouldExist)
    {
        return new FileArgument(dir, shouldExist);
    }

    @Override
    public File parse(StringReader reader) throws CommandSyntaxException
    {
        if (this.dir == null)
        {
            throw NO_DIRECTORY.create();
        }

        final int startPos = reader.getCursor();

        while (reader.canRead() && reader.peek() != ' ')
        {
           reader.skip();
        }

        String name = reader.getString().substring(startPos, reader.getCursor());

        if (name.isEmpty())
        {
            throw EMPTY_FILE_NAME.create();
        }

        File file = new File(this.dir, name);

        if (this.shouldExist && file.exists() == false)
        {
            throw NO_SUCH_FILE_EXCEPTION.create(file.getAbsolutePath());
        }

        return file;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        if (this.dir == null)
        {
            return builder.buildFuture();
        }

        return CommandUtils.suggestIterable(CommandUtils.getFileNames(this.dir, CommandUtils.FILTER_FILES), builder);
    }
}
