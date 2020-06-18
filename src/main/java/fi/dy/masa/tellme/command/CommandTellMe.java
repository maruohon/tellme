package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class CommandTellMe
{
    public static void registerServerCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        register(dispatcher, "tellme", 4);
    }

    public static void registerClientCommand(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        register(dispatcher, "ctellme", 0);
    }

    protected static void register(CommandDispatcher<ServerCommandSource> dispatcher, String baseCommandName, int permissionLevel)
    {
        dispatcher.register(
                CommandManager.literal(baseCommandName)
                    .requires((src) -> src.hasPermissionLevel(permissionLevel))
                    .executes(c -> { c.getSource().sendFeedback(new LiteralText("/tellme help"), false); return 0; })
                    .then(SubCommandBatchRun.registerSubCommand(dispatcher))
                    .then(SubCommandBiome.registerSubCommand(dispatcher))
                    .then(SubCommandBiomeLocate.registerSubCommand(dispatcher))
                    .then(SubCommandBiomeStats.registerSubCommand(dispatcher))
                    .then(SubCommandBlockStats.registerSubCommand(dispatcher))
                    .then(SubCommandCopyToClipboard.registerSubCommand(dispatcher))
                    .then(SubCommandDump.registerSubCommand(dispatcher))
                    .then(SubCommandDumpJson.registerSubCommand(dispatcher))
                    .then(SubCommandHolding.registerSubCommand(dispatcher))
                    .then(SubCommandLoaded.registerSubCommand(dispatcher))
                    .then(SubCommandLocate.registerSubCommand(dispatcher))
                    .then(SubCommandLookingAt.registerSubCommand(dispatcher))
        );
    }
}
