package fi.dy.masa.tellme.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.tellme.util.ItemInfo;

public class SubCommandHolding extends SubCommand
{
    public SubCommandHolding(CommandTellme baseCommand)
    {
        super(baseCommand);
        this.subSubCommands.add("dump");
        this.subSubCommands.add("print");
    }

    @Override
    public String getName()
    {
        return "holding";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        super.execute(server, sender, args);

        if (args.length == 1 && sender instanceof EntityPlayer)
        {
            if (args[0].equals("dump") || args[0].equals("print"))
            {
                this.handleHeldObject((EntityPlayer) sender, args[0].equals("dump"));
            }
        }
    }

    private void handleHeldObject(EntityPlayer player, boolean dumpToFile)
    {
        ItemStack stack = player.getHeldItemMainhand();

        if (stack != null)
        {
            ItemInfo.printItemInfo(player, stack, dumpToFile);
        }
    }
}
