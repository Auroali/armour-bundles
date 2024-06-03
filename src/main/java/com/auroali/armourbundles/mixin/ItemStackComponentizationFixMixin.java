package com.auroali.armourbundles.mixin;

import com.auroali.armourbundles.ArmourProfile;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
    @Inject(method = "fixStack", at = @At("HEAD"))
    private static void armourbundles$fixOldBundles(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, CallbackInfo ci) {
        if(data.itemEquals("armourbundles:armour_bundle")) {
            data.moveToComponent("Inv", "armourbundles:armour_bundle_inventory", dynamic.createList(Stream.empty()));
            data.moveToComponent("CurrentProfile", "armourbundles:current_profile");
            fixProfiles(data, dynamic, "Profiles", "armourbundles:profiles");
        }
    }

    @Unique
    private static void fixProfiles(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, String nbtKey, String componentId) {
        OptionalDynamic<?> optionalDynamic = data.getAndRemove(nbtKey);
        List<Map<String, Dynamic<?>>> profiles = optionalDynamic.asList(d -> d.asMap(k -> k.asString(""), v -> v));
        //Map<String, ItemStack> map = optionalDynamic.asMap(k -> k.asString(""), v -> ItemStack.CODEC.parse(v).getOrThrow());
        if(profiles.isEmpty())
            return;
        Dynamic<?> output = dynamic.createList(profiles.stream().map(m -> dynamic.createList(m.keySet().stream().map(stack -> {
            int index = EquipmentSlot.byName(stack).ordinal();
            return dynamic.emptyMap()                    .set("slot", dynamic.createInt(index))
                    .set("stack", m.get(stack));
        }))));
        data.setComponent(componentId, output);
    }
}