package fi.dy.masa.tellme.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tellme.command.ClientCommandHandler;
import net.minecraft.util.TabCompleter;

@Mixin(TabCompleter.class)
public class MixinTabCompleter
{
    @Shadow
    protected List<String> completions;

    @Shadow
    public void complete() {}

    @Inject(method = "requestCompletions(Ljava/lang/String;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/NetHandlerPlayClient;sendPacket(Lnet/minecraft/network/Packet;)V"))
    protected void onRequestCompletions(String prefix, CallbackInfo ci)
    {
        if (prefix.length() >= 1)
        {
            ClientCommandHandler.INSTANCE.autoComplete(prefix);
        }
    }

    @Redirect(method = "setCompletions", at = @At(
                value = "INVOKE",
                target = "Lorg/apache/commons/lang3/StringUtils;getCommonPrefix([Ljava/lang/String;)V"
        ))
    public String addCompletionsAndRemoveFormattingCodes(String... newCompl)
    {
        String[] complete = ClientCommandHandler.INSTANCE.latestAutoComplete;

        // Add our command completions
        if (complete != null)
        {
            for (String s : complete)
            {
                if (s.isEmpty() == false)
                {
                    this.completions.add(s);
                }
            }

            newCompl = com.google.common.collect.ObjectArrays.concat(complete, newCompl, String.class);
        }

        String prefix = org.apache.commons.lang3.StringUtils.getCommonPrefix(newCompl);

        return net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(prefix);
    }

    @ModifyArg(method = "complete", at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/gui/GuiTextField;writeText(Ljava/lang/String;)V"
            ))
    public String removeFormattingCodes2(String text)
    {
        return net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(text);
    }
}
