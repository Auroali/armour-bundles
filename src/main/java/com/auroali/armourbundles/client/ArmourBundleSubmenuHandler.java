package com.auroali.armourbundles.client;

import com.auroali.armourbundles.ArmourBundles;
import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;
import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.auroali.armourbundles.common.network.ArmourBundleScrollC2S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;
import org.jspecify.annotations.NonNull;

public class ArmourBundleSubmenuHandler implements ItemSlotMouseAction {
    protected final Minecraft client;
    protected final ScrollWheelHandler scroller;

    public ArmourBundleSubmenuHandler(Minecraft client) {
        this.client = client;
        this.scroller = new ScrollWheelHandler();
    }

    @Override
    public boolean matches(Slot slot) {
        return slot.getItem().has(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
    }

    @Override
    public boolean onMouseScrolled(double horizontal, double vertical, int slotId, ItemStack item) {
        ArmourBundleContentsComponent component = item.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        int size = component.getStacks().size();
        if (size == 0)
            return false;

        Vector2i scroll = this.scroller.onMouseScroll(horizontal, vertical);
        int amount = scroll.y == 0 ? -scroll.x : scroll.y;
        if (amount == 0)
            return true;

        int selectedIndex = component.getSelected();
        int newIndex = ScrollWheelHandler.getNextScrollWheelSelection(amount, selectedIndex, size);
        if (newIndex != selectedIndex)
            this.sendPacket(slotId, newIndex, item);
        return true;
    }

    @Override
    public void onStopHovering(Slot slot) {
        this.sendPacket(slot.index, -1, slot.getItem());
    }

    @Override
    public void onSlotClicked(@NonNull Slot slot, @NonNull ContainerInput containerInput) {
        if (containerInput == ContainerInput.QUICK_MOVE || containerInput == ContainerInput.SWAP)
            this.onStopHovering(slot);
    }

    private void sendPacket(int slotId, int newIndex, ItemStack stack) {
        ClientPlayNetworking.send(new ArmourBundleScrollC2S(slotId, newIndex));
        ArmourBundleItem.setSelectedStack(stack, newIndex);
    }
}
