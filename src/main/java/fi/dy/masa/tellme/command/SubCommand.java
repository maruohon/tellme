package fi.dy.masa.tellme.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;

public abstract class SubCommand implements ISubCommand
{
    protected final CommandTellme baseCommand;
    protected final ArrayList<String> subSubCommands = new ArrayList<>();
    private final Map<String, String> help = new HashMap<>();
    private final Map<String, String> usage = new HashMap<>();

    public SubCommand(CommandTellme baseCommand)
    {
        this.baseCommand = baseCommand;
        this.subSubCommands.add("help");
    }

    public CommandTellme getBaseCommand()
    {
        return this.baseCommand;
    }

    @Override
    public List<String> getSubCommands()
    {
        return this.subSubCommands;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 1 || (args.length == 2 && args[0].equals("help")))
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, this.getSubCommands());
        }

        return Collections.emptyList();
    }

    @Override
    public ITextComponent getHelp()
    {
        return new TextComponentString("Available sub-commands are: " + String.join(", ", this.subSubCommands));
    }

    protected String getSubCommandUsagePre()
    {
        return "/" + this.getBaseCommand().getName() + " " + this.getName();
    }

    protected void addSubCommandUsage(String subCommand, String usage)
    {
        this.usage.put(subCommand, usage);
    }

    protected String getSubCommandUsage(String subCommand)
    {
        return this.usage.get(subCommand);
    }

    protected void addSubCommandHelp(String subCommand, String help)
    {
        this.help.put(subCommand, help);
    }

    protected String getSubCommandHelp(String subCommand)
    {
        return this.help.get(subCommand);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // "/tellme sub-command"
        if (args.length == 0)
        {
            sender.sendMessage(this.getHelp());
        }
        // "/tellme sub-command [help|unknown]"
        else if (args.length == 1)
        {
            if (args[0].equals("help"))
            {
                sender.sendMessage(this.getHelp());
            }
            else if (this.subSubCommands.contains(args[0]) == false)
            {
                throw new WrongUsageException("Unknown sub command '" + args[0] + "'");
            }
        }
        // "/tellme sub-command help sub-sub-command"
        else if (args.length == 2 && args[0].equals("help"))
        {
            if (args[1].equals("help"))
            {
                this.sendMessage(sender, "Gives information about the sub-commands");
            }
            else if (this.subSubCommands.contains(args[1]))
            {
                String str = this.getSubCommandHelp(args[1]);

                if (StringUtils.isBlank(str) == false)
                {
                    this.sendMessage(sender, str);
                }
            }
            else
            {
                throw new WrongUsageException("Unknown sub command '" + args[1] + "'");
            }
        }
    }

    protected void sendMessage(ICommandSender sender, String message, Object... args)
    {
        sender.sendMessage(new TextComponentTranslation(message, args));
    }

    public static String[] dropFirstStrings(String[] input, int toDrop)
    {
        if (toDrop >= input.length)
        {
            return new String[0];
        }

        String[] arr = new String[input.length - toDrop];
        System.arraycopy(input, toDrop, arr, 0, input.length - toDrop);
        return arr;
    }

    public static void sendClickableLinkMessage(ICommandSender sender, String messageKey, File file)
    {
        ITextComponent name = new TextComponentString(file.getName());

        //if (TellMe.proxy.isSinglePlayer())
        {
            name.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()));
            name.getStyle().setUnderlined(Boolean.valueOf(true));
        }

        sender.sendMessage(new TextComponentTranslation(messageKey, name));
    }

    public static BlockPos parseBlockPos(BlockPos base, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        double x = CommandBase.parseDouble(base.getX(), args[startIndex    ], -30000000, 30000000, centerBlock);
        double y = CommandBase.parseDouble(base.getY(), args[startIndex + 1], 0, 256, false);
        double z = CommandBase.parseDouble(base.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock);
        return new BlockPos(x, y, z);
    }

    public static boolean isInteger(String arg)
    {
        try
        {
            Integer.parseInt(arg);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static int getNumberOfTrailingIntegers(String[] args)
    {
        return getNumberOfTrailingIntegers(args, args.length - 1);
    }

    /**
     * Returns the number of consecutive strings in the given array that are integer numbers,
     * starting from the index <b>lastIndex</b> and checking backwards.
     * @param args
     * @param lastIndex
     * @return the number of consecutive trailing integers
     */
    public static int getNumberOfTrailingIntegers(String[] args, int lastIndex)
    {
        int count = 0;

        try
        {
            for (int i = lastIndex; i >= 0; i--)
            {
                Integer.parseInt(args[i]);
                count++;
            }
        }
        catch (NumberFormatException e) {}

        return count;
    }
}
