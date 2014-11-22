package fi.dy.masa.tellme.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import fi.dy.masa.tellme.util.BiomeInfo;

public class SubCommandBiome implements ISubCommand
{
    @Override
    public String getCommandName()
    {
        return "biome";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            return;
        }

        // "/tellme biome"
        if (args.length == 1 || (args.length == 2 && args[1].equals("current") == true))
        {
            if (sender instanceof EntityPlayer)
            {
                BiomeInfo.printCurrentBiomeInfoToChat((EntityPlayer)sender);
            }

            return;
        }

        if (args.length == 2)
        {
            if (args[1].equals("dump") == true)
            {
                BiomeInfo.dumpBiomeListToFile();
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.output.to.file.cfgdir")));
            }
            else if (args[1].equals("list") == true)
            {
                BiomeInfo.printBiomeListToLogger();
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.output.to.console")));
            }
            else
            {
                throw new WrongUsageException(StatCollector.translateToLocal("info.command.unknown.subcommand"));
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 2)
        {
            ArrayList<String> cmds = new ArrayList<String>();

            cmds.add("current");
            cmds.add("dump");
            cmds.add("list");

            return cmds;
        }

        return null;
    }
}
