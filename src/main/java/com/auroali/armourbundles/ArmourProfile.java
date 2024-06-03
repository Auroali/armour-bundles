package com.auroali.armourbundles;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// Represents an individual armour profile
public class ArmourProfile {
    public static final Codec<ArmourProfile> CODEC = Codec.mapPair(Codec.INT.fieldOf("slot"), ItemStack.CODEC.fieldOf("stack"))
            .codec()
            .listOf()
            .xmap(ArmourProfile::new, p -> {
                List<Pair<Integer, ItemStack>> pairs = new ArrayList<>();
                for(int i = 0; i < p.armorPieces.length; i++) {
                    if(!p.armorPieces[i].isEmpty()) pairs.add(new Pair<>(i, p.armorPieces[i]));
                }
                return pairs;
            });

    private final ItemStack[] armorPieces = new ItemStack[EquipmentSlot.values().length];

    public ArmourProfile() {
        Arrays.fill(armorPieces, ItemStack.EMPTY);
    }
    public ArmourProfile(List<Pair<Integer, ItemStack>> stacks) {
        Arrays.fill(armorPieces, ItemStack.EMPTY);
        for(Pair<Integer, ItemStack> stackPair : stacks) {
            armorPieces[stackPair.getFirst()] = stackPair.getSecond();
        }
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

    @Deprecated
    public NbtCompound writeToNbt(NbtCompound compound) {
        return new NbtCompound();
    }

    @Deprecated
    public static ArmourProfile fromNbt(NbtCompound compound) {
        return null;
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
