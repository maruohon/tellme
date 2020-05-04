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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import fi.dy.masa.tellme.TellMe;
import fi.dy.masa.tellme.command.argument.FileArgument;
import fi.dy.masa.tellme.config.Configs;

public class SubCommandBatchRun
{
    public static String getHelp(CommandTellMe baseCommand)
    {
        return "Runs commands from files inside config/tellme/batch_commands/";
    }

    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        LiteralCommandNode<CommandSource> subCommandRootNode = Commands.literal("batch-run").build();

        ArgumentCommandNode<CommandSource, File> fileNameNode = Commands.argument("file_name", FileArgument.getFor(getBatchDirectory(), true))
                .executes(c -> execute(dispatcher, c, c.getArgument("file_name", File.class))).build();

        subCommandRootNode.addChild(fileNameNode);

        return subCommandRootNode;
    }

    private static int execute(CommandDispatcher<CommandSource> dispatcher, CommandContext<CommandSource> ctx, File file) throws CommandSyntaxException
    {
        if (file != null && file.exists() && file.canRead())
        {
            runBatchCommands(null, ctx.getSource(), file);
        }
        else
        {
            CommandUtils.throwException("Usage: /tellme batch-run <filename>");
        }

        return 1;
    }

    private static void runBatchCommands(CommandDispatcher<CommandSource> dispatcher, CommandSource source, File batchFile) throws CommandSyntaxException
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
        List<String> lines = new ArrayList<String>();

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
        File cfgDir = Configs.dumpOutputDir;
        return new File(cfgDir, "batch_commands");
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
