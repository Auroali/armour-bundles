package com.auroali.armourbundles.datafixer.fixers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;

import java.util.HashMap;

public class ArmourBundleReworkDatafix extends DataFix {
    public ArmourBundleReworkDatafix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    public Dynamic<?> fixComponent(Dynamic<?> stack, Dynamic<?> components) {
        Dynamic<?> contents = stack.createMap(ImmutableMap.<Dynamic<?>, Dynamic<?>>builder()
          .put(stack.createString("stacks"), components.get("armourbundles:armour_bundle_inventory").orElseEmptyList())
          .build());

        return stack.set("components", components
          .set("armourbundles:armour_bundle_contents", contents)
          .remove("armourbundles:armour_bundle_inventory")
          .remove("armourbundles:current_profile")
          .remove("armourbundles:profiles")
        );
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead(
          "Update Armour Bundle Components",
          this.getInputSchema().getType(TypeReferences.ITEM_STACK),
          this.getOutputSchema().getType(TypeReferences.ITEM_STACK),
          dynamic -> {
              if (!dynamic.get("id").asString().resultOrPartial().map(s -> s.equals("armourbundles:armour_bundle")).orElse(false))
                  return dynamic;

              return fixComponent(dynamic, dynamic.get("components").orElseEmptyMap());
          }
        );
    }
}
