package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.command.CommandManager;
import fi.dy.masa.tellme.command.CommandTellMe;

@Mixin(CommandManager.class)
public class MixinCommandManager
{
    @Inject(method = "<init>(Lnet/minecraft/server/command/CommandManager$RegistrationEnvironment;)V", at = @At("RETURN"))
    private void onInit(CommandManager.RegistrationEnvironment environment, CallbackInfo ci)
    {
        CommandTellMe.registerServerCommand(((CommandManager) (Object) this).getDispatcher());
    }
}
