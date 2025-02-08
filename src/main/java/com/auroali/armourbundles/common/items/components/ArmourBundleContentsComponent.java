package com.auroali.armourbundles.common.items.components;

import com.auroali.armourbundles.ArmourBundles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.List;

public class ArmourBundleContentsComponent implements TooltipData {
    public static final int MAX_STACKS = 4;
    public static final Codec<ArmourBundleContentsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ItemStack.CODEC.listOf().fieldOf("stacks").forGetter(ArmourBundleContentsComponent::getStacks),
      Codec.INT.fieldOf("selected").forGetter(ArmourBundleContentsComponent::getSelected)
    ).apply(instance, ArmourBundleContentsComponent::new));
    public static final PacketCodec<RegistryByteBuf, ArmourBundleContentsComponent> PACKET_CODEC = PacketCodec.tuple(
      ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
      ArmourBundleContentsComponent::getStacks,
      PacketCodecs.VAR_INT,
      ArmourBundleContentsComponent::getSelected,
      ArmourBundleContentsComponent::new
    );

    public static final ArmourBundleContentsComponent DEFAULT = new ArmourBundleContentsComponent(List.of(), -1);
    protected List<ItemStack> stacks;
    protected int selected;

    protected ArmourBundleContentsComponent(List<ItemStack> stacks, int selected) {
        this.stacks = stacks;
        this.selected = selected;
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

    public Builder builder() {
        return new Builder(new ArrayList<>(this.stacks), this.selected);
    }

    @Override
    public int hashCode() {
        return ItemStack.listHashCode(this.stacks);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof ArmourBundleContentsComponent component)
            return ItemStack.stacksEqual(this.stacks, component.stacks);

        return false;
    }

    public static boolean isInsertableStack(ItemStack stack) {
        return stack.isIn(ArmourBundles.VALID_ARMOUR_BUNDLE_ITEMS);
    }

    public static class Builder {
        private final List<ItemStack> stacks;
        private int selected;

        public Builder(List<ItemStack> stacks, int selected) {
            this.stacks = stacks;
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

        public ArmourBundleContentsComponent build() {
            return new ArmourBundleContentsComponent(this.stacks, this.selected);
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
