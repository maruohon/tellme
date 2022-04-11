package fi.dy.masa.tellme.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.tellme.LiteModTellMe;

public class SubCommandBatchRun extends SubCommand
{
    public static final FilenameFilter FILTER_FILES = new FilenameFilter()
    {
        @Override
        public boolean accept(File pathName, String name)
        {
            return (new File(pathName, name)).isFile();
        }
    };

    public SubCommandBatchRun(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.addSubCommandHelp("_generic", "Runs commands from files inside config/tellme/batch_commands/");
    }

    @Override
    public String getName()
    {
        return "batch-run";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length != 1)
        {
            return Collections.emptyList();
        }

        return CommandBase.getListOfStringsMatchingLastWord(args, this.getExistingBatchFileNames());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 1)
        {
            this.runBatchCommands(server, sender, args[0]);
        }
        else
        {
            throw new WrongUsageException(this.getSubCommandUsagePre() + " batch-run <filename>");
        }
    }

    private void runBatchCommands(MinecraftServer server, ICommandSender sender, String fileName)
    {
        File batchFile = this.getBatchCommandFile(fileName);

        if (batchFile != null)
        {
            ICommandManager manager = server.getCommandManager();
            List<String> commands = this.getCommands(batchFile);

            for (String command : commands)
            {
                LiteModTellMe.logger.info("Running a command: '{}'", command);
                manager.executeCommand(sender, command);
            }
        }
    }

    private List<String> getCommands(File batchFile)
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
            LiteModTellMe.logger.warn("Failed to read commands from a batch file '{}'", batchFile.getAbsolutePath());
        }

        return lines;
    }

    @Nullable
    private File getBatchCommandFile(String fileName)
    {
        File cfgDir = new File(LiteModTellMe.configDirPath);
        File batchFile = new File(new File(cfgDir, "batch_commands"), fileName);

        return batchFile.exists() && batchFile.isFile() ? batchFile : null;
    }

    private List<String> getExistingBatchFileNames()
    {
        File dir = new File(new File(LiteModTellMe.configDirPath), "batch_commands");

        if (dir.isDirectory())
        {
            String[] names = dir.list(FILTER_FILES);
            return Arrays.asList(names);
        }

        return Collections.emptyList();
    }
}
