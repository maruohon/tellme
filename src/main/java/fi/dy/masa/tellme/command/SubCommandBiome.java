package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import fi.dy.masa.tellme.datadump.BiomeDump;

public class SubCommandBiome
{
    public static CommandNode<CommandSourceStack> registerSubCommand(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        return Commands.literal("biome").executes(c -> execute(c.getSource())).build();
    }

    private static int execute(CommandSourceStack source) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity instanceof Player)
        {
            BiomeDump.printCurrentBiomeInfoToChat((Player) entity);
            return 1;
        }

        throw CommandUtils.NOT_A_PLAYER_EXCEPTION.create();
    }
}
