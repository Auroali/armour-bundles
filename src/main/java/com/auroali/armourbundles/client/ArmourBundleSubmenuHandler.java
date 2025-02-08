package com.auroali.armourbundles.client;

import com.auroali.armourbundles.ArmourBundles;
import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.auroali.armourbundles.common.items.components.ArmourBundleContentsComponent;
import com.auroali.armourbundles.common.network.ArmourBundleScrollC2S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.client.input.Scroller;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.joml.Vector2i;

public class ArmourBundleSubmenuHandler implements TooltipSubmenuHandler {
    protected final MinecraftClient client;
    protected final Scroller scroller;

    public ArmourBundleSubmenuHandler(MinecraftClient client) {
        this.client = client;
        this.scroller = new Scroller();
    }

    @Override
    public boolean isApplicableTo(Slot slot) {
        return slot.getStack().contains(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
    }

    @Override
    public boolean onScroll(double horizontal, double vertical, int slotId, ItemStack item) {
        ArmourBundleContentsComponent component = item.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        int size = component.getStacks().size();
        if (size == 0)
            return false;

        Vector2i scroll = this.scroller.update(horizontal, vertical);
        int amount = scroll.y == 0 ? -scroll.x : scroll.y;
        if (amount == 0)
            return true;

        int selectedIndex = component.getSelected();
        int newIndex = Scroller.scrollCycling(amount, selectedIndex, size);
        if (newIndex != selectedIndex)
            this.sendPacket(slotId, newIndex, item);
        return true;
    }

    @Override
    public void reset(Slot slot) {
        this.sendPacket(slot.id, -1, slot.getStack());
    }

    @Override
    public void onMouseClick(Slot slot, SlotActionType actionType) {
        if (actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP)
            this.reset(slot);
    }

    private void sendPacket(int slotId, int newIndex, ItemStack stack) {
        ClientPlayNetworking.send(new ArmourBundleScrollC2S(slotId, newIndex));
        ArmourBundleItem.setSelectedStack(stack, newIndex);
    }
}
