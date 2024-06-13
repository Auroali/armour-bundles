package com.auroali.armourbundles.items;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ArmourBundleInventory(List<ItemStack> stacks) {
    public static ArmourBundleInventory DEFAULT = new ArmourBundleInventory(Collections.emptyList());
    public static ArmourBundleInventory create() {
        return DEFAULT;
    }

    public static ArmourBundleInventory create(List<ItemStack> stacks) {
        if(stacks.isEmpty())
            return DEFAULT;
        return new ArmourBundleInventory(List.copyOf(stacks));
    }

    public static ArmourBundleInventory create(ArmourBundleInventory inventory, ItemStack stack) {
        List<ItemStack> stacks = new ArrayList<>(inventory.stacks().size() + 1);
        stacks.add(stack);
        stacks.addAll(inventory.stacks);
        return create(stacks);
    }

    public ArmourBundleInventory remove(ItemStack stack) {
        ArrayList<ItemStack> list = new ArrayList<>(stacks);
        list.remove(stack);
        if(list.isEmpty())
            return DEFAULT;
        return create(list);
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof ArmourBundleInventory i && ItemStack.stacksEqual(stacks, i.stacks));
    }

    @Override
    public int hashCode() {
        return ItemStack.listHashCode(this.stacks);
    }
}
