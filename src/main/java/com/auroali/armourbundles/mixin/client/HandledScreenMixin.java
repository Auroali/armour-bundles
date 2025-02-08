package com.auroali.armourbundles.mixin.client;

import com.auroali.armourbundles.client.ArmourBundleSubmenuHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void addTooltipSubmenuHandler(TooltipSubmenuHandler handler);

    @Inject(method = "init", at = @At("RETURN"))
    public void armourbundles$addArmourBundleTooltipSubmenu(CallbackInfo ci) {
        this.addTooltipSubmenuHandler(new ArmourBundleSubmenuHandler(this.client));
    }
}
