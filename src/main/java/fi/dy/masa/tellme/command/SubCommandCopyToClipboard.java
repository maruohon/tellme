package fi.dy.masa.tellme.command;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class SubCommandCopyToClipboard extends SubCommand
{
    public SubCommandCopyToClipboard(CommandTellme baseCommand)
    {
        super(baseCommand);
    }

    @Override
    public String getName()
    {
        return "copy-to-clipboard";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (Desktop.isDesktopSupported())
        {
            String str = String.join(" ", args);
            StringSelection stringSelection = new StringSelection(str);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);

            if (sender instanceof EntityPlayer)
            {
                ((EntityPlayer) sender).sendStatusMessage(new TextComponentString("Copied " + str), true);
            }
            else
            {
                sender.sendMessage(new TextComponentString("Copied " + str));
            }
        }
    }
}
