package fi.dy.masa.tellme.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import fi.dy.masa.tellme.util.BiomeInfo;

public class SubCommandBiome extends SubCommand
{
    public SubCommandBiome()
    {
        super();
        this.subSubCommands.add("current");
        this.subSubCommands.add("dump");
        this.subSubCommands.add("list");
    }

    @Override
    public String getCommandName()
    {
        return "biome";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        super.processCommand(sender, args);

        if (args.length == 2)
        {
            if (args[1].equals("current") == true)
            {
                if (sender instanceof EntityPlayer)
                {
                    BiomeInfo.printCurrentBiomeInfoToChat((EntityPlayer)sender);
                }
            }
            else if (args[1].equals("dump") == true)
            {
                BiomeInfo.dumpBiomeListToFile();
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.output.to.file.cfgdir")));
            }
            else if (args[1].equals("list") == true)
            {
                BiomeInfo.printBiomeListToLogger();
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.output.to.console")));
            }
        }
    }
}
