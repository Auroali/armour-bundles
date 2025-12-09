package com.auroali.armourbundles.mixin.client;

import com.auroali.armourbundles.client.ArmourBundleSubmenuHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    protected abstract void addItemSlotMouseAction(ItemSlotMouseAction handler);

    @Inject(method = "init", at = @At("RETURN"))
    public void armourbundles$addArmourBundleTooltipSubmenu(CallbackInfo ci) {
        this.addItemSlotMouseAction(new ArmourBundleSubmenuHandler(this.minecraft));
    }
}
