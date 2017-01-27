package fi.dy.masa.tellme.command;

import java.util.List;
import net.minecraft.command.CommandBase;
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
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length == 3)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "adjacent");
        }

        return super.getTabCompletions(server, sender, args);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length >= 2 && sender instanceof EntityPlayer)
        {
            if (args[1].equals("dump") || args[1].equals("print"))
            {
                this.handleLookedAtObject((EntityPlayer) sender, args.length == 3 && args[2].equals("adjacent"), args[1].equals("dump"));
            }
        }
    }

    private void handleLookedAtObject(EntityPlayer player, boolean adjacent, boolean dumpToFile)
    {
        RayTraceResult result = RayTraceUtils.rayTraceFromPlayer(player.getEntityWorld(), player, true);

        if (result != null)
        {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                BlockInfo.getBlockInfoFromRayTracedTarget(player.getEntityWorld(), player, adjacent, dumpToFile);
            }
            else if (result.typeOfHit == RayTraceResult.Type.ENTITY)
            {
                EntityInfo.printEntityInfo(player, result.entityHit, dumpToFile);
            }
        }
    }
}
