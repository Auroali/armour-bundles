package com.auroali.armourbundles;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Arrays;
import java.util.Optional;

// Represents an individual armour profile
public class ArmourProfile {
    private final ItemStack[] armorPieces = new ItemStack[EquipmentSlot.values().length];

    public ArmourProfile() {
        Arrays.fill(armorPieces, ItemStack.EMPTY);
    }
    public void setArmourSlot(EquipmentSlot slot, ItemStack stack) {
        armorPieces[slot.ordinal()] = stack;
    }

    public boolean matches(EquipmentSlot slot, ItemStack stack) {
        if(armorPieces[slot.ordinal()].isEmpty())
            return false;

        ItemStack itemInSlot = armorPieces[slot.ordinal()];
        // if the enchantments, name and item id are all equal then we assume they match
        return itemInSlot.getName().equals(stack.getName())
                && itemInSlot.isOf(stack.getItem())
                && itemInSlot.getEnchantments().equals(stack.getEnchantments());
    }

    public Optional<ItemStack> getItemInSlot(EquipmentSlot slot) {
        return armorPieces[slot.ordinal()].isEmpty() ? Optional.empty() : Optional.of(armorPieces[slot.ordinal()]);
    }

    public NbtCompound writeToNbt(NbtCompound compound) {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = armorPieces[slot.ordinal()];
            if(stack.isEmpty())
                continue;
            compound.put(slot.getName(), stack.writeNbt(new NbtCompound()));
        }
        return compound;
    }

    public static ArmourProfile fromNbt(NbtCompound compound) {
        ArmourProfile profile = new ArmourProfile();
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            if(!compound.contains(slot.getName()))
                continue;
            ItemStack stack = ItemStack.fromNbt(compound.getCompound(slot.getName()));
            profile.armorPieces[slot.ordinal()] = stack;
        }
        return profile;
    }

    public static ArmourProfile generateFrom(PlayerEntity entity) {
        ArmourProfile profile = new ArmourProfile();
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            if(slot.getType() != EquipmentSlot.Type.ARMOR)
                continue;

            ItemStack stack = entity.getEquippedStack(slot);
            if(stack.isEmpty())
                continue;

            profile.setArmourSlot(slot, stack);
        }

        return profile;
    }
}
