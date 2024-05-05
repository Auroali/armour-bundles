package com.auroali.armourbundles.items;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ArmourBundleInventory(List<ItemStack> stacks) {
    public static ArmourBundleInventory create() {
        return new ArmourBundleInventory(Collections.emptyList());
    }

    public static ArmourBundleInventory create(List<ItemStack> stacks) {
        return new ArmourBundleInventory(Collections.unmodifiableList(stacks));
    }

    public static ArmourBundleInventory create(ArmourBundleInventory inventory, ItemStack stack) {
        List<ItemStack> stacks = new ArrayList<>(inventory.stacks().size() + 1);
        stacks.addAll(inventory.stacks);
        stacks.add(stack);
        return create(stacks);
    }

    public ArmourBundleInventory remove(ItemStack stack) {
        ArrayList<ItemStack> list = new ArrayList<>(stacks);
        list.remove(stack);
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
