package fi.dy.masa.tellme.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import fi.dy.masa.tellme.command.CommandTellMe;

@Mixin(CommandManager.class)
public class MixinCommandManager
{
    @Inject(method = "<init>", remap = false,
            at = @At(value = "INVOKE",
                     target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V"))
    private void onInit(CommandManager.RegistrationEnvironment environment,
                        CommandRegistryAccess commandRegistryAccess,
                        CallbackInfo ci)
    {
        CommandTellMe.registerServerCommand(((CommandManager) (Object) this).getDispatcher());
    }
}
