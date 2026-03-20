package com.auroali.armourbundles;

import com.auroali.armourbundles.client.ArmourBundleContentsTooltipComponent;
import com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel;
import com.auroali.armourbundles.client.render.item.property.ArmourBundleHasSelectedItemProperty;
import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;
import com.auroali.armourbundles.common.network.CycleEquippedC2S;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;

public class ArmourBundlesClient implements ClientModInitializer {
    public static KeyMapping.Category CATEGORY = KeyMapping.Category.register(ArmourBundles.id("keybinds"));

    public static KeyMapping EQUIP_PREV = new KeyMapping(
      "key.armourprofiles.equip_prev",
      InputConstants.Type.KEYSYM,
      InputConstants.KEY_Y,
      CATEGORY
    );
    public static KeyMapping EQUIP_NEXT = new KeyMapping(
      "key.armourprofiles.equip_next",
      InputConstants.Type.KEYSYM,
      InputConstants.KEY_U,
      CATEGORY
    );

    int timer;

    @Override
    public void onInitializeClient() {
        //ModelPredicateProviderRegistry.register(ArmourBundles.ARMOUR_BUNDLE, Identifier.of("filled"), (stack, world, entity, seed) -> ArmourBundles.ARMOUR_BUNDLE.getFillPercent(stack));
        KeyMappingHelper.registerKeyMapping(EQUIP_PREV);
        KeyMappingHelper.registerKeyMapping(EQUIP_NEXT);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (timer > 0) {
                timer--;
                return;
            }
            while (EQUIP_PREV.consumeClick()) {
                ClientPlayNetworking.send(new CycleEquippedC2S(false));
                timer = 10;
            }
            while (EQUIP_NEXT.consumeClick()) {
                ClientPlayNetworking.send(new CycleEquippedC2S(true));
                timer = 10;
            }
        });

        ClientTooltipComponentCallback.EVENT.register(tooltipData -> {
            if (tooltipData instanceof ArmourBundleContentsComponent data)
                return new ArmourBundleContentsTooltipComponent(data);
            return null;
        });

        ItemModels.ID_MAPPER.put(ArmourBundles.id("armour_bundle/selected_item"), ArmourBundleSelectedItemModel.Unbaked.CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(ArmourBundles.id("armour_bundle/has_selected_item"), ArmourBundleHasSelectedItemProperty.CODEC);
        //ModelLoadingPlugin.register(ctx -> ctx..addModels(ArmourBundles.OPEN_FRONT_TEXTURE, ArmourBundles.OPEN_BACK_TEXTURE));
    }
}
