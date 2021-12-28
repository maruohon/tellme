package fi.dy.masa.tellme.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import fi.dy.masa.tellme.config.Configs;

public class CommandReloadConfig
{
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("TellMe: failed to reload the config!"));

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(
                Commands.literal("tellme-reload")
                    .requires((src) -> src.hasPermission(4))
                    .executes((src) -> reloadConfig(src.getSource())));
     }

     private static int reloadConfig(CommandSource source) throws CommandSyntaxException
     {
         if (Configs.reloadConfig())
         {
             source.sendSuccess(new StringTextComponent("TellMe config reloaded"), false);
             return 1;
         }

         throw FAILED_EXCEPTION.create();
     }
}
