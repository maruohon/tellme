package fi.dy.masa.tellme.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import fi.dy.masa.tellme.datadump.BiomeDump;

public class SubCommandBiome extends SubCommand
{
    public SubCommandBiome(CommandTellme baseCommand)
    {
        super(baseCommand);

        this.addSubCommandHelp("_generic", "Prints information about the current biome to chat");
    }

    @Override
    public String getName()
    {
        return "biome";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            if (sender instanceof EntityPlayer)
            {
                BiomeDump.printCurrentBiomeInfoToChat((EntityPlayer) sender);
            }
        }
        else
        {
            throw new CommandException("Too many arguments");
        }
    }
}
