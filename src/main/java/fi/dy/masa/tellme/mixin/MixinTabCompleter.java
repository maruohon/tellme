package fi.dy.masa.tellme.mixin;

import java.util.List;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tellme.command.ClientCommandHandler;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.TabCompleter;

@Mixin(TabCompleter.class)
public class MixinTabCompleter
{
    @Shadow
    protected boolean requestedCompletions;
    @Shadow
    protected boolean didComplete;
    @Shadow
    protected List<String> completions;
    @Shadow
    @Final
    protected GuiTextField textField;
    @Shadow
    protected int completionIdx;

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

    @Inject(method = "setCompletions", at = @At("HEAD"), cancellable = true)
    private void onSetCompletions(String[] newCompl, CallbackInfo ci)
    {
        if (this.requestedCompletions)
        {
            this.didComplete = false;
            this.completions.clear();

            String[] complete = ClientCommandHandler.INSTANCE.latestAutoComplete;

            if (complete != null)
            {
                newCompl = com.google.common.collect.ObjectArrays.concat(complete, newCompl, String.class);
            }

            for (String s : newCompl)
            {
                if (!s.isEmpty())
                {
                    this.completions.add(s);
                }
            }

            String s1 = this.textField.getText().substring(this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false));
            String s2 = org.apache.commons.lang3.StringUtils.getCommonPrefix(newCompl);
            s2 = net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(s2);

            if (!s2.isEmpty() && !s1.equalsIgnoreCase(s2))
            {
                this.textField.deleteFromCursor(0);
                this.textField.deleteFromCursor(this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false) - this.textField.getCursorPosition());
                this.textField.writeText(s2);
            }
            else if (!this.completions.isEmpty())
            {
                this.didComplete = true;
                this.complete();
            }

            ci.cancel();
        }
    }

    // This injection point is before the completionIdx gets incremented at the end of the complete() method.
    @Inject(method = "complete", at = @At(
                //value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiTextField;writeText(Ljava/lang/String;)V"
                value = "FIELD", target = "Lnet/minecraft/util/TabCompleter;completionIdx:I", opcode = Opcodes.PUTFIELD, ordinal = 2
            ),
            cancellable = true)
    private void onComplete(CallbackInfo ci)
    {
        String cleaned = this.completions.get(this.completionIdx++);
        cleaned = net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(cleaned);
        this.textField.writeText(cleaned);
        ci.cancel();
    }
}
