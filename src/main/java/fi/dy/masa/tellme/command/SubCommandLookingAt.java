package fi.dy.masa.tellme.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.RayTraceResult;
import fi.dy.masa.tellme.util.BlockInfo;
import fi.dy.masa.tellme.util.EntityInfo;
import fi.dy.masa.tellme.util.RayTraceUtils;

public class SubCommandLookingAt extends SubCommand
{
    public SubCommandLookingAt(CommandTellme baseCommand)
    {
        super(baseCommand);
        this.subSubCommands.add("dump");
        this.subSubCommands.add("print");
    }

    @Override
    public String getName()
    {
        return "lookingat";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length == 2 && sender instanceof EntityPlayer)
        {
            if (args[1].equals("dump") || args[1].equals("print"))
            {
                this.handleLookedAtObject((EntityPlayer) sender, args[1].equals("dump"));
            }
        }
    }

    private void handleLookedAtObject(EntityPlayer player, boolean dumpToFile)
    {
        RayTraceResult result = RayTraceUtils.rayTraceFromPlayer(player.getEntityWorld(), player, true);

        if (result != null)
        {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                BlockInfo.getBlockInfoFromRayTracedTarget(player.getEntityWorld(), player, dumpToFile);
            }
            else if (result.typeOfHit == RayTraceResult.Type.ENTITY)
            {
                EntityInfo.printEntityInfo(player, result.entityHit, dumpToFile);
            }
        }
    }
}
