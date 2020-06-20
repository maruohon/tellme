package fi.dy.masa.tellme.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.argument.FileArgument;
import fi.dy.masa.tellme.reference.Reference;

public class SubCommandBatchRun
{
    public static String getHelp(CommandTellMe baseCommand)
    {
        return "Runs commands from files inside config/tellme/batch_commands/";
    }

    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralCommandNode<ServerCommandSource> subCommandRootNode = CommandManager.literal("batch-run").build();

        ArgumentCommandNode<ServerCommandSource, File> fileNameNode = CommandManager.argument("file_name", FileArgument.getFor(getBatchDirectory(), true))
                .executes(c -> execute(dispatcher, c, c.getArgument("file_name", File.class))).build();

        subCommandRootNode.addChild(fileNameNode);

        return subCommandRootNode;
    }

    private static int execute(CommandDispatcher<ServerCommandSource> dispatcher, CommandContext<ServerCommandSource> ctx, File file) throws CommandSyntaxException
    {
        if (file != null && file.exists() && file.canRead())
        {
            runBatchCommands(dispatcher, ctx.getSource(), file);
        }
        else
        {
            CommandUtils.throwException("Usage: /tellme batch-run <filename>");
        }

        return 1;
    }

    private static void runBatchCommands(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandSource source, File batchFile) throws CommandSyntaxException
    {
        List<String> commands = getCommands(batchFile);

        for (String command : commands)
        {
            TellMe.logger.info("Running a command: '{}'", command);
            dispatcher.execute(command, source);
        }
    }

    private static List<String> getCommands(File batchFile)
    {
        List<String> lines = new ArrayList<>();

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(batchFile.getAbsolutePath())));
            String line;

            while ((line = br.readLine()) != null)
            {
                // Exclude lines starting with '#' (comments)
                if (StringUtils.isBlank(line) == false && line.charAt(0) != '#')
                {
                    lines.add(line);
                }
            }

            br.close();
        }
        catch (IOException e)
        {
            TellMe.logger.warn("Failed to read commands from a batch file '{}'", batchFile.getAbsolutePath());
        }

        return lines;
    }

    private static File getBatchDirectory()
    {
        return new File(new File(TellMe.dataProvider.getConfigDirectory(), Reference.MOD_ID), "batch_commands");
    }

    @Nullable
    private static File getBatchCommandFile(String fileName)
    {
        File batchFile = new File(getBatchDirectory(), fileName);
        return batchFile.exists() && batchFile.isFile() ? batchFile : null;
    }

    public static List<String> getBatchFileNames()
    {
        return CommandUtils.getFileNames(getBatchDirectory(), CommandUtils.FILTER_FILES);
    }
}
