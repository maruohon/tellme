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

        if (args.length == 1)
        {
            if (sender instanceof EntityPlayer)
            {
                BiomeDump.printCurrentBiomeInfoToChat((EntityPlayer) sender);
            }
        }
    }
}
