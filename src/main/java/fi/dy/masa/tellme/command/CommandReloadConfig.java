package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import fi.dy.masa.tellme.config.Configs;

public class CommandReloadConfig
{
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableComponent("TellMe: failed to reload the config!"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("tellme-reload")
                    .requires((src) -> src.hasPermission(4))
                    .executes((src) -> reloadConfig(src.getSource())));
     }

     private static int reloadConfig(CommandSourceStack source) throws CommandSyntaxException
     {
         if (Configs.reloadConfig())
         {
             source.sendSuccess(new TextComponent("TellMe config reloaded"), false);
             return 1;
         }

         throw FAILED_EXCEPTION.create();
     }
}
