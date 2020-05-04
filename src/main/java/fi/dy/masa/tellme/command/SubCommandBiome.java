package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
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

        if (entity != null)
        {
            BiomeDump.printCurrentBiomeInfoToChat(entity);
            return 1;
        }

        throw CommandUtils.NOT_AN_ENTITY_EXCEPTION.create();
    }
}
