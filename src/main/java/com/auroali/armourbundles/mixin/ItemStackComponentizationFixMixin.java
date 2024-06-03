package com.auroali.armourbundles.mixin;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
    @Inject(method = "fixStack", at = @At("HEAD"))
    private static void armourbundles$fixOldBundles(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, CallbackInfo ci) {
        if(data.itemEquals("armourbundles:armour_bundle")) {
            data.moveToComponent("Inv", "armourbundles:armour_bundle_inventory", dynamic.createList(Stream.empty()));
        }
    }
}
