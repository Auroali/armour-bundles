package com.auroali.armourbundles.common.components;

import com.auroali.armourbundles.ArmourBundles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class ArmourBundleContentsComponent implements TooltipData {
    public static final int MAX_STACKS = 4;
    public static final EnumSet<EquipmentSlot> VALID_SLOTS = EnumSet.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    public static final ArmourBundleContentsComponent DEFAULT = new ArmourBundleContentsComponent(List.of(), new EnumMap<>(EquipmentSlot.class), -1);
    public static final Codec<ArmourBundleContentsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ItemStack.CODEC.listOf().fieldOf("stacks").forGetter(ArmourBundleContentsComponent::getStacks),
      Codec.unboundedMap(EquipmentSlot.CODEC, ItemStack.CODEC)
        .xmap(EnumMap::new, Function.identity())
        .validate(map -> {
            for (EquipmentSlot slot : map.keySet()) {
                if (!VALID_SLOTS.contains(slot))
                    return DataResult.error(() -> "Invalid slot " + slot + " for bound item");
            }
            return DataResult.success(map);
        })
        .optionalFieldOf("boundItems", DEFAULT.boundEquipment)
        .forGetter(ArmourBundleContentsComponent::getBoundEquipment)
    ).apply(instance, ArmourBundleContentsComponent::new));
    public static final PacketCodec<RegistryByteBuf, ArmourBundleContentsComponent> PACKET_CODEC = PacketCodec.tuple(
      ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
      ArmourBundleContentsComponent::getStacks,
      PacketCodecs.map(i -> new EnumMap<>(EquipmentSlot.class), EquipmentSlot.PACKET_CODEC, ItemStack.PACKET_CODEC),
      ArmourBundleContentsComponent::getBoundEquipment,
      PacketCodecs.VAR_INT,
      ArmourBundleContentsComponent::getSelected,
      ArmourBundleContentsComponent::new
    );

    protected List<ItemStack> stacks;
    protected EnumMap<EquipmentSlot, ItemStack> boundEquipment;
    protected int selected;

    protected ArmourBundleContentsComponent(List<ItemStack> stacks, EnumMap<EquipmentSlot, ItemStack> boundEquipment) {
        this(stacks, boundEquipment, -1);
    }

    protected ArmourBundleContentsComponent(List<ItemStack> stacks, EnumMap<EquipmentSlot, ItemStack> boundEquipment, int selected) {
        this.stacks = stacks;
        this.selected = selected;
        this.boundEquipment = boundEquipment;
    }

    public List<ItemStack> getStacks() {
        return this.stacks;
    }

    public int getSelected() {
        return this.selected;
    }

    public boolean isEmpty() {
        return this.stacks.isEmpty();
    }

    public boolean hasSelected() {
        return this.selected >= 0 && this.selected < this.stacks.size();
    }

    public ItemStack getSelectedStack() {
        return this.stacks.get(this.selected);
    }

    public EnumMap<EquipmentSlot, ItemStack> getBoundEquipment() {
        return this.boundEquipment;
    }

    public Builder builder() {
        return new Builder(new ArrayList<>(this.stacks), new EnumMap<>(this.boundEquipment), this.selected);
    }

    @Override
    public int hashCode() {
        return ItemStack.listHashCode(this.stacks);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof ArmourBundleContentsComponent component) {
            if (!ItemStack.stacksEqual(this.stacks, component.stacks))
                return false;
            if (this.boundEquipment.size() != component.boundEquipment.size())
                return false;
            for (EquipmentSlot slot : this.boundEquipment.keySet()) {
                if (!ItemStack.areEqual(this.boundEquipment.getOrDefault(slot, ItemStack.EMPTY), component.boundEquipment.getOrDefault(slot, ItemStack.EMPTY))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isInsertableStack(ItemStack stack) {
        return stack.isIn(ArmourBundles.VALID_ARMOUR_BUNDLE_ITEMS);
    }

    public static class Builder {
        private final List<ItemStack> stacks;
        private final EnumMap<EquipmentSlot, ItemStack> boundItems;
        private int selected;

        public Builder(List<ItemStack> stacks, EnumMap<EquipmentSlot, ItemStack> boundItems, int selected) {
            this.stacks = stacks;
            this.boundItems = boundItems;
            this.selected = selected;
        }

        public int add(ItemStack stack) {
            if (!isInsertableStack(stack))
                return 0;

            int i = this.getInsertionIndex(stack);
            if (i == -1) {
                return this.insertIntoNewSlot(stack);
            }

            ItemStack current = this.stacks.get(i);
            int count = Math.min(stack.getCount(), current.getMaxCount() - current.getCount());
            if (count <= 0) {
                return this.insertIntoNewSlot(stack);
            }

            this.stacks.set(i, current.copyWithCount(current.getCount() + count));
            ItemStack remaining = stack.split(count);
            int remainingCount = remaining.getCount();
            if (!remaining.isEmpty() && this.insertIntoNewSlot(remaining) == remainingCount)
                return count + remainingCount;

            return count;
        }

        public void setSelected(int i) {
            this.selected = this.selected != i && this.selected <= this.stacks.size() ? i : -1;
        }

        public ItemStack removeSelected() {
            if (this.stacks.isEmpty())
                return ItemStack.EMPTY;

            int index = this.selected;
            this.selected = -1;
            if (index == -1 || index >= this.stacks.size()) {
                return this.stacks.removeFirst();
            }

            return this.stacks.remove(index);
        }

        public void bindItem(EquipmentSlot slot, ItemStack stack) {
            this.boundItems.put(slot, stack.copy());
        }

        public void clearBindings() {
            this.boundItems.clear();
        }

        public boolean hasBinding(EquipmentSlot slot) {
            return this.boundItems.containsKey(slot);
        }

        public ArmourBundleContentsComponent build() {
            return new ArmourBundleContentsComponent(this.stacks, this.boundItems, this.selected);
        }

        private int insertIntoNewSlot(ItemStack stack) {
            if (this.stacks.size() >= MAX_STACKS)
                return 0;

            int count = stack.getCount();
            this.stacks.addFirst(stack.split(stack.getCount()));
            return count;
        }

        private int getInsertionIndex(ItemStack stack) {
            if (!stack.isStackable())
                return -1;

            for (int i = 0; i < this.stacks.size(); i++) {
                if (ItemStack.areItemsAndComponentsEqual(stack, this.stacks.get(i)))
                    return i;
            }
            return -1;
        }
    }
}
