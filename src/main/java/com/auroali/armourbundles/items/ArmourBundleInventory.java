package com.auroali.armourbundles.items;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PairCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ArmourBundleInventory(List<ItemStack> stacks, int selectedSlot) {
    public static ArmourBundleInventory DEFAULT = new ArmourBundleInventory(Collections.emptyList(), 0);
    public static Codec<ArmourBundleInventory> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.CODEC.listOf().fieldOf("items").forGetter(ArmourBundleInventory::stacks),
                    Codec.INT.fieldOf("selectedSlot").forGetter(ArmourBundleInventory::selectedSlot)
            ).apply(instance, ArmourBundleInventory::new)
    );
    public static PacketCodec<RegistryByteBuf, ArmourBundleInventory> PACKET_CODEC = PacketCodec
            .tuple(
                    ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()), ArmourBundleInventory::stacks,
                    PacketCodecs.VAR_INT, ArmourBundleInventory::selectedSlot,
                    ArmourBundleInventory::new
            );
//    public static ArmourBundleInventory create() {
//        return DEFAULT;
//    }

//    public static ArmourBundleInventory create(List<ItemStack> stacks) {
//        if(stacks.isEmpty())
//            return DEFAULT;
//        return new ArmourBundleInventory(List.copyOf(stacks), 0);
//    }
//
//    public static ArmourBundleInventory create(ArmourBundleInventory inventory, ItemStack stack) {
//        List<ItemStack> stacks = new ArrayList<>(inventory.stacks().size() + 1);
//        stacks.add(stack);
//        stacks.addAll(inventory.stacks);
//        return new ArmourBundleInventory(stacks, Math.min(inventory.selectedSlot(), stacks.size() - 1));
//    }

//    public ArmourBundleInventory remove(ItemStack stack) {
//        ArrayList<ItemStack> list = new ArrayList<>(stacks);
//        list.remove(stack);
//        if(list.isEmpty())
//            return DEFAULT;
//        return new ArmourBundleInventory(list, Math.min(selectedSlot(), list.size() - 1));
//    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || (o instanceof ArmourBundleInventory i && selectedSlot == i.selectedSlot && ItemStack.stacksEqual(stacks, i.stacks));
    }

    @Override
    public int hashCode() {
        return ItemStack.listHashCode(this.stacks);
    }

    public static Builder builder() {
        return new Builder().setStacks(new ArrayList<>());
    }

    public static Builder builder(ArmourBundleInventory inventory) {
        return new Builder().setStacks(inventory.stacks).setSelectedSlot(inventory.selectedSlot);
    }

    public static class Builder {
        List<ItemStack> stacks;
        int selectedSlot;
        int maxSize = ArmourBundleItem.MAX_SIZE;

        public Builder setStacks(List<ItemStack> stacks) {
            this.stacks = new ArrayList<>(stacks);
            return this;
        }

        public Builder setSelectedSlot(int slot) {
            this.selectedSlot = slot;
            return this;
        }

        public int getSelectedSlot() {
            return this.selectedSlot;
        }

        public boolean hasStack(ItemStack stack) {
            return stacks.contains(stack);
        }

        public ItemStack removeStack(int slot) {
            ItemStack stack = stacks.remove(slot);
            return stack == null ? ItemStack.EMPTY : stack;
        }

        public boolean removeStack(ItemStack stack) {
            return stacks.remove(stack);
        }

        public ItemStack getStack(int slot) {
            return stacks.get(slot);
        }

        public boolean insertStack(ItemStack stack, int slot) {
            if(stacks.size() >= maxSize)
                return false;
            stacks.add(slot, stack);
            return true;
        }

        public int getSize() {
            return stacks.size();
        }

        public ArmourBundleInventory build() {
            if(this.stacks.isEmpty())
                return DEFAULT;
            return new ArmourBundleInventory(this.stacks, this.selectedSlot);
        }
    }
}
