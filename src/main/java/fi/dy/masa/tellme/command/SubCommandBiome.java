package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import fi.dy.masa.tellme.datadump.BiomeDump;

public class SubCommandBiome
{
    public static CommandNode<CommandSource> registerSubCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        return Commands.literal("biome").executes(c -> execute(c.getSource())).build();
    }

    private static int execute(CommandSource source) throws CommandSyntaxException
    {
        Entity entity = source.getEntity();

        if (entity instanceof PlayerEntity)
        {
            BiomeDump.printCurrentBiomeInfoToChat((PlayerEntity) entity);
            return 1;
        }

        throw CommandUtils.NOT_A_PLAYER_EXCEPTION.create();
    }
}
