package com.auroali.armourbundles.common.items;

import com.auroali.armourbundles.ArmourBundles;
import com.auroali.armourbundles.common.items.components.ArmourBundleContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ArmourBundleItem extends Item {
    private final Identifier openBack;
    private final Identifier openFront;

    public ArmourBundleItem(Identifier openBack, Identifier openFront, Item.Settings settings) {
        super(settings);
        this.openBack = openBack;
        this.openFront = openFront;
    }

    public static boolean hasSelectedStack(ItemStack stack) {
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return false;
        return component.hasSelected();
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return false;

        ArmourBundleContentsComponent.Builder builder = component.builder();
        ItemStack other = slot.getStack();
        if (clickType == ClickType.LEFT && !other.isEmpty()) {
            if (builder.add(other) == 0) {
                playInsertFailSound(player);
                return true;
            }

            playInsertSound(player);
            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        if (clickType == ClickType.RIGHT && other.isEmpty()) {
            ItemStack removed = builder.removeSelected();
            if (removed.isEmpty())
                return false;

            ItemStack remaining = slot.insertStack(removed);
            if (!remaining.isEmpty())
                builder.add(remaining);

            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
            setSelectedStack(stack, -1);
            return false;
        }

        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return false;

        ArmourBundleContentsComponent.Builder builder = component.builder();
        if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
            if (!slot.canTakePartial(player) || builder.add(otherStack) == 0) {
                playInsertFailSound(player);
                return true;
            }

            playInsertSound(player);
            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
            if (!slot.canTakePartial(player))
                return true;

            ItemStack removed = builder.removeSelected();
            if (removed.isEmpty())
                return true;

            playRemoveOneSound(player);
            cursorStackReference.set(removed);

            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        setSelectedStack(stack, -1);
        return false;
    }

    public static void setSelectedStack(ItemStack stack, int index) {
        ArmourBundleContentsComponent contents = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (contents == null)
            return;

        ArmourBundleContentsComponent.Builder builder = contents.builder();
        builder.setSelected(index);
        stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
    }

    public static ItemStack getSelectedStack(ItemStack stack) {
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null || !component.hasSelected())
            return ItemStack.EMPTY;

        return component.getSelectedStack();
    }

    private void onContentChanged(PlayerEntity user) {
        ScreenHandler screenHandler = user.currentScreenHandler;
        if (screenHandler != null) {
            screenHandler.onContentChanged(user.getInventory());
        }
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        return component == null ? super.getTooltipData(stack) : Optional.of(component);
    }

    public Identifier getOpenBack() {
        return this.openBack;
    }

    public Identifier getOpenFront() {
        return this.openFront;
    }

    // from the vanilla bundle
    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

}
