package com.auroali.armourbundles.datafixer;

import com.auroali.dfuhooks.v1.api.DFUHooksItemComponentHook;
import com.auroali.dfuhooks.v1.api.DFUHooksSchemaHook;
import com.auroali.dfuhooks.v1.api.SchemaRegistry;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.entity.EquipmentSlot;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ABItemComponentFix extends DFUHooksItemComponentHook {
    @Override
    public void runHook(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, int displayFlags) {
        if (data.itemEquals("armourbundles:armour_bundle")) {
            data.moveToComponent("Inv", "armourbundles:armour_bundle_inventory", dynamic.createList(Stream.empty()));
            data.moveToComponent("CurrentProfile", "armourbundles:current_profile");
            fixProfiles(data, dynamic, "Profiles", "armourbundles:profiles");
        }
    }

    private static void fixProfiles(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, String nbtKey, String componentId) {
        OptionalDynamic<?> optionalDynamic = data.getAndRemove(nbtKey);
        List<Map<String, Dynamic<?>>> profiles = optionalDynamic.asList(d -> d.asMap(k -> k.asString(""), v -> v));
        //Map<String, ItemStack> map = optionalDynamic.asMap(k -> k.asString(""), v -> ItemStack.CODEC.parse(v).getOrThrow());
        if (profiles.isEmpty())
            return;
        Dynamic<?> output = dynamic.createList(profiles.stream().map(m -> dynamic.createList(m.keySet().stream().map(stack -> {
            int index = EquipmentSlot.byName(stack).ordinal();
            return dynamic.emptyMap().set("slot", dynamic.createInt(index))
              .set("stack", m.get(stack));
        }))));
        data.setComponent(componentId, output);
    }
}
