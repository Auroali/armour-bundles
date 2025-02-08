package com.auroali.armourbundles;

import com.auroali.armourbundles.client.ArmourBundleContentsTooltipComponent;
import com.auroali.armourbundles.common.items.components.ArmourBundleContentsComponent;
import com.auroali.armourbundles.common.network.EquipSlotC2SPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ArmourBundlesClient implements ClientModInitializer {
    public static KeyBinding PROFILE_1;
    public static KeyBinding PROFILE_2;
    public static KeyBinding PROFILE_3;

    @Override
    public void onInitializeClient() {
        //ModelPredicateProviderRegistry.register(ArmourBundles.ARMOUR_BUNDLE, Identifier.of("filled"), (stack, world, entity, seed) -> ArmourBundles.ARMOUR_BUNDLE.getFillPercent(stack));

        PROFILE_1 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
          "key.armourprofiles.select.1",
          InputUtil.Type.KEYSYM,
          GLFW.GLFW_KEY_Y,
          "category.armourprofiles.profiles"
        ));
        PROFILE_2 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
          "key.armourprofiles.select.2",
          InputUtil.Type.KEYSYM,
          GLFW.GLFW_KEY_U,
          "category.armourprofiles.profiles"
        ));
        PROFILE_3 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
          "key.armourprofiles.select.3",
          InputUtil.Type.KEYSYM,
          GLFW.GLFW_KEY_I,
          "category.armourprofiles.profiles"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (PROFILE_1.wasPressed()) {
                ClientPlayNetworking.send(new EquipSlotC2SPacket(0));
            }
            while (PROFILE_2.wasPressed()) {
                ClientPlayNetworking.send(new EquipSlotC2SPacket(1));
            }
            while (PROFILE_3.wasPressed()) {
                ClientPlayNetworking.send(new EquipSlotC2SPacket(2));
            }
        });

        TooltipComponentCallback.EVENT.register(tooltipData -> {
            if (tooltipData instanceof ArmourBundleContentsComponent data)
                return new ArmourBundleContentsTooltipComponent(data);
            return null;
        });

        ModelLoadingPlugin.register(ctx -> ctx.addModels(ArmourBundles.OPEN_FRONT_TEXTURE, ArmourBundles.OPEN_BACK_TEXTURE));
    }
}
