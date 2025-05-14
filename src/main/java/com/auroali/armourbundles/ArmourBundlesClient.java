package com.auroali.armourbundles;

import com.auroali.armourbundles.client.ArmourBundleContentsTooltipComponent;
import com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel;
import com.auroali.armourbundles.client.render.item.property.ArmourBundleHasSelectedItemProperty;
import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;
import com.auroali.armourbundles.common.network.CycleEquippedC2S;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.item.model.ItemModelTypes;
import net.minecraft.client.render.item.property.bool.BooleanProperties;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ArmourBundlesClient implements ClientModInitializer {
    public static KeyBinding EQUIP_PREV = new KeyBinding(
      "key.armourprofiles.equip_prev",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_Y,
      "category.armourprofiles.profiles"
    );
    public static KeyBinding EQUIP_NEXT = new KeyBinding(
      "key.armourprofiles.equip_next",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_U,
      "category.armourprofiles.profiles"
    );

    int timer;

    @Override
    public void onInitializeClient() {
        //ModelPredicateProviderRegistry.register(ArmourBundles.ARMOUR_BUNDLE, Identifier.of("filled"), (stack, world, entity, seed) -> ArmourBundles.ARMOUR_BUNDLE.getFillPercent(stack));
        KeyBindingHelper.registerKeyBinding(EQUIP_PREV);
        KeyBindingHelper.registerKeyBinding(EQUIP_NEXT);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (timer > 0) {
                timer--;
                return;
            }
            while (EQUIP_PREV.wasPressed()) {
                ClientPlayNetworking.send(new CycleEquippedC2S(false));
                timer = 10;
            }
            while (EQUIP_NEXT.wasPressed()) {
                ClientPlayNetworking.send(new CycleEquippedC2S(true));
                timer = 10;
            }
        });

        TooltipComponentCallback.EVENT.register(tooltipData -> {
            if (tooltipData instanceof ArmourBundleContentsComponent data)
                return new ArmourBundleContentsTooltipComponent(data);
            return null;
        });

        ItemModelTypes.ID_MAPPER.put(ArmourBundles.id("armour_bundle/selected_item"), ArmourBundleSelectedItemModel.Unbaked.CODEC);
        BooleanProperties.ID_MAPPER.put(ArmourBundles.id("armour_bundle/has_selected_item"), ArmourBundleHasSelectedItemProperty.CODEC);
        //ModelLoadingPlugin.register(ctx -> ctx..addModels(ArmourBundles.OPEN_FRONT_TEXTURE, ArmourBundles.OPEN_BACK_TEXTURE));
    }
}
