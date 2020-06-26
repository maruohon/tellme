package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import fi.dy.masa.tellme.datadump.BiomeDump;

public class SubCommandBiome
{
    public static CommandNode<ServerCommandSource> registerSubCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        return CommandManager.literal("biome").executes(c -> execute(c.getSource())).build();
    }

    private static int execute(ServerCommandSource source) throws CommandSyntaxException
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
