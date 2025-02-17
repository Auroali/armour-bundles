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

    /**
     * @return all stacks stored in this bundle
     */
    public List<ItemStack> getStacks() {
        return this.stacks;
    }

    /**
     * @return the current selected slot, or -1 if nothing is selected
     */
    public int getSelected() {
        return this.selected;
    }

    /**
     * @return if this bundle has no items
     */
    public boolean isEmpty() {
        return this.stacks.isEmpty();
    }

    /**
     * @return if this bundle has a selected stack
     */
    public boolean hasSelected() {
        return this.selected >= 0 && this.selected < this.stacks.size();
    }

    /**
     * @return the currently selected item stack
     */
    public ItemStack getSelectedStack() {
        return this.stacks.get(this.selected);
    }

    /**
     * Returns all currently bound equipment
     *
     * @return currently bound equipment, in the form of a map that associates each stack to its relevant slot
     */
    public EnumMap<EquipmentSlot, ItemStack> getBoundEquipment() {
        return this.boundEquipment;
    }

    /**
     * Creates a builder to allow modifying this component
     *
     * @return the new builder
     */
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

        protected Builder(List<ItemStack> stacks, EnumMap<EquipmentSlot, ItemStack> boundItems, int selected) {
            this.stacks = stacks;
            this.boundItems = boundItems;
            this.selected = selected;
        }

        /**
         * Adds an item stack to this builder
         *
         * @param stack the stack to add
         * @return the amount of items that were successfully added
         */
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

        /**
         * Sets the selected slot of this builder
         *
         * @param i the currently selected slot
         */
        public void setSelected(int i) {
            this.selected = this.selected != i && this.selected <= this.stacks.size() ? i : -1;
        }

        /**
         * Removes the currently selected stack from this builder
         *
         * @return the currently selected item stack, or empty if nothing is selected
         */
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

        /**
         * Binds an item stack to a specific slot
         *
         * @param slot  the slot to bind to
         * @param stack the stack to bind
         */
        public void bindItem(EquipmentSlot slot, ItemStack stack) {
            this.boundItems.put(slot, stack.copy());
        }

        /**
         * Clears all currently bound equipment
         */
        public void clearBindings() {
            this.boundItems.clear();
        }

        /**
         * Check if an equipment slot has a bound item
         *
         * @param slot the slot to check
         * @return if an item is bound to that slot
         */
        public boolean hasBinding(EquipmentSlot slot) {
            return this.boundItems.containsKey(slot);
        }

        /**
         * @return an ArmourBundleContentsComponent with the information from this builder
         */
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
