package fi.dy.masa.tellme.command;

import java.util.ArrayList;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

public class SubCommandBlockStats extends SubCommand
{
    public SubCommandBlockStats()
    {
        super();
    }

    @Override
    public String getCommandName()
    {
        return "blockstats";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        super.processCommand(sender, args);

        if (sender instanceof EntityPlayer == false)
        {
            throw new WrongUsageException(StatCollector.translateToLocal("info.subcommand.blockstats.notplayer"));
        }

        // "/tellme blockstats <playername> <x-distance> <y-distance> <z-distance> [blocktype blocktype ...]"
        if (args.length < 5)
        {
            String str = StatCollector.translateToLocal("info.command.usage") + " '/"
                + CommandTellme.instance.getCommandName() + " blockstats <playername> <x-distance> <y-distance> <z-distance> [blocktype blocktype ...]'";
            sender.addChatMessage(new ChatComponentText(str));

            return;
        }

        if (args.length > 5)
        {
            ArrayList<String> blockFilter = new ArrayList<String>();
            for (int i = 5; i < args.length; ++i)
            {
                blockFilter.add(args[i]);
            }
        }

        // FIXME
        sender.addChatMessage(new ChatComponentText("WIP"));
    }
}
