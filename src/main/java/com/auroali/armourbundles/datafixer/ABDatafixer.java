package com.auroali.armourbundles.datafixer;

import com.auroali.armourbundles.datafixer.fixers.ArmourBundleReworkDatafix;
import com.auroali.dfuhooks.v2.api.ModDFUInitializer;
import com.auroali.dfuhooks.v2.api.SchemaBuilderProvider;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ABDatafixer extends ModDFUInitializer {
    @Override
    public void init(SchemaBuilderProvider schemaBuilderProvider) {
        schemaBuilderProvider.createOrModifySchemaBuilder(4081, builder -> {
            builder.addDataFixer(schema -> new ArmourBundleReworkDatafix(schema, false));
        });
    }

    @Override
    public void fixItemComponents(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
        if (itemStackData.is("armourbundles:armour_bundle")) {
            itemStackData.moveTagToComponent("Inv", "armourbundles:armour_bundle_inventory", dynamic.createList(Stream.empty()));
            itemStackData.moveTagToComponent("CurrentProfile", "armourbundles:current_profile");
            fixProfiles(itemStackData, dynamic, "Profiles", "armourbundles:profiles");
        }
    }

    private static void fixProfiles(ItemStackComponentizationFix.ItemStackData data, Dynamic<?> dynamic, String nbtKey, String componentId) {
        OptionalDynamic<?> optionalDynamic = data.removeTag(nbtKey);
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
