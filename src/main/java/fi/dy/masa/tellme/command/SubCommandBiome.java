package fi.dy.masa.tellme.command;

import java.io.File;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import fi.dy.masa.tellme.util.BiomeInfo;
import fi.dy.masa.tellme.util.DataDump;

public class SubCommandBiome extends SubCommand
{
    public SubCommandBiome(CommandTellme baseCommand)
    {
        super(baseCommand);
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
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        super.processCommand(sender, args);

        if (args.length == 2)
        {
            if (args[1].equals("current"))
            {
                if (sender instanceof EntityPlayer)
                {
                    BiomeInfo.printCurrentBiomeInfoToChat((EntityPlayer)sender);
                }
            }
            else if (args[1].equals("dump"))
            {
                File f = DataDump.dumpDataToFile("biome_dump", BiomeInfo.getBiomeList());
                sender.addChatMessage(new ChatComponentText("Output written to file " + f.getName()));
            }
            else if (args[1].equals("list"))
            {
                BiomeInfo.printBiomeListToLogger();
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("info.output.to.console")));
            }
        }
    }
}
