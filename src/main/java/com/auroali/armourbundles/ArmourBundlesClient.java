package com.auroali.armourbundles;

import com.auroali.armourbundles.items.tooltipdata.ArmourBundleTooltipComponent;
import com.auroali.armourbundles.items.tooltipdata.ArmourBundleTooltipData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ArmourBundlesClient implements ClientModInitializer {
    public static KeyBinding PROFILE_1;
    public static KeyBinding PROFILE_2;
    public static KeyBinding PROFILE_3;

    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(ArmourBundles.ARMOUR_BUNDLE, new Identifier("filled"), (stack, world, entity, seed) -> ArmourBundles.ARMOUR_BUNDLE.getFillPercent(stack));

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
            if(tooltipData instanceof ArmourBundleTooltipData data)
                return new ArmourBundleTooltipComponent(data.inventory());
            return null;
        });
    }
}
