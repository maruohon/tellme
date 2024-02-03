package fi.dy.masa.tellme.command;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;

import malilib.util.position.BlockPos;
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
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 2)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "adjacent");
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length >= 1 && (args[0].equals("dump") || args[0].equals("print")) && sender instanceof EntityPlayer)
        {
            this.handleLookedAtObject((EntityPlayer) sender, args.length == 2 && args[1].equals("adjacent"), args[0].equals("dump"));
        }
    }

    private void handleLookedAtObject(EntityPlayer player, boolean adjacent, boolean dumpToFile)
    {
        RayTraceResult trace = RayTraceUtils.getRayTraceFromEntity(player.getEntityWorld(), player, true, 10d);

        if (trace.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            BlockInfo.getBlockInfoFromRayTracedTarget(player.getEntityWorld(), player, trace, adjacent, dumpToFile);
        }
        else if (trace.typeOfHit == RayTraceResult.Type.ENTITY)
        {
            EntityInfo.printEntityInfo(player, trace.entityHit, dumpToFile);
        }
        else
        {
            player.sendMessage(new TextComponentString("Not currently looking at anything within range"));
        }
    }
}
