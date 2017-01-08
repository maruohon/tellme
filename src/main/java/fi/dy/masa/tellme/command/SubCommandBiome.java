package fi.dy.masa.tellme.command;

import java.io.File;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
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
    public String getName()
    {
        return "biome";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

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
                this.sendMessage(sender, "tellme.info.output.to.file", f.getName());
            }
            else if (args[1].equals("list"))
            {
                BiomeInfo.printBiomeListToLogger();
                this.sendMessage(sender, "tellme.info.output.to.console");
            }
        }
    }
}
