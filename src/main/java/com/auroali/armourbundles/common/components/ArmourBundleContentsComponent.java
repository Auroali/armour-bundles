package com.auroali.armourbundles.common.components;

import com.auroali.armourbundles.ArmourBundles;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public class ArmourBundleContentsComponent implements TooltipComponent {
    public static final int MAX_STACKS = 4;
    public static final EnumSet<EquipmentSlot> VALID_SLOTS = EnumSet.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    public static final ArmourBundleContentsComponent DEFAULT = new ArmourBundleContentsComponent(List.of(), new EnumMap<>(EquipmentSlot.class), -1);
    // codec for the bound items map
    public static final Codec<EnumMap<EquipmentSlot, ItemStackTemplate>> BOUND_ITEMS_CODEC = Codec.unboundedMap(EquipmentSlot.CODEC, ItemStackTemplate.CODEC)
      .xmap(EnumMap::new, Function.identity())
      .validate(map -> {
          for (EquipmentSlot slot : map.keySet()) {
              if (!VALID_SLOTS.contains(slot)) {
                  EnumMap<EquipmentSlot, ItemStackTemplate> newMap = new EnumMap<>(EquipmentSlot.class);
                  map.forEach((k, v) -> {
                      if (VALID_SLOTS.contains(k))
                          newMap.put(k, v);
                  });
                  return DataResult.error(
                    () -> "Invalid slot " + slot + " for bound item",
                    newMap
                  );
              }
          }
          return DataResult.success(map);
      });

    // codec for the contents themselves
    public static final Codec<ArmourBundleContentsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ItemStackTemplate.CODEC.listOf().fieldOf("stacks").forGetter(ArmourBundleContentsComponent::getStacks),
      BOUND_ITEMS_CODEC
        .optionalFieldOf("boundItems", DEFAULT.boundEquipment)
        .forGetter(ArmourBundleContentsComponent::getBoundEquipment)
    ).apply(instance, ArmourBundleContentsComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmourBundleContentsComponent> PACKET_CODEC = StreamCodec.composite(
      ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()),
      ArmourBundleContentsComponent::getStacks,
      ByteBufCodecs.map(i -> new EnumMap<>(EquipmentSlot.class), EquipmentSlot.STREAM_CODEC, ItemStackTemplate.STREAM_CODEC),
      ArmourBundleContentsComponent::getBoundEquipment,
      ByteBufCodecs.VAR_INT,
      ArmourBundleContentsComponent::getSelected,
      ArmourBundleContentsComponent::new
    );

    protected List<ItemStackTemplate> stacks;
    protected EnumMap<EquipmentSlot, ItemStackTemplate> boundEquipment;
    protected int selected;

    protected ArmourBundleContentsComponent(List<ItemStackTemplate> stacks, EnumMap<EquipmentSlot, ItemStackTemplate> boundEquipment) {
        this(stacks, boundEquipment, -1);
    }

    protected ArmourBundleContentsComponent(List<ItemStackTemplate> stacks, EnumMap<EquipmentSlot, ItemStackTemplate> boundEquipment, int selected) {
        this.stacks = stacks;
        this.selected = selected;
        this.boundEquipment = boundEquipment;
    }

    /**
     * @return all stacks stored in this bundle
     */
    public List<ItemStackTemplate> getStacks() {
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
    public ItemStackTemplate getSelectedStack() {
        return this.stacks.get(this.selected);
    }

    /**
     * Returns all currently bound equipment
     *
     * @return currently bound equipment, in the form of a map that associates each stack to its relevant slot
     */
    public EnumMap<EquipmentSlot, ItemStackTemplate> getBoundEquipment() {
        return this.boundEquipment;
    }

    /**
     * Creates a builder to allow modifying this component
     *
     * @return the new builder
     */
    public Builder builder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return this.stacks.hashCode() + 31 * this.boundEquipment.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof ArmourBundleContentsComponent component) {
            if (!this.stacks.equals(component.stacks))
                return false;
            if (!this.boundEquipment.equals(component.boundEquipment))
                return false;
            return true;
        }
        return false;
    }

    public static boolean isInsertableStack(ItemStack stack) {
        return stack.is(ArmourBundles.VALID_ARMOUR_BUNDLE_ITEMS);
    }

    public static class Builder {
        private final List<ItemStack> stacks;
        private final EnumMap<EquipmentSlot, ItemStack> boundItems;
        private int selected;

        protected Builder(ArmourBundleContentsComponent contents) {
            this.stacks = new ArrayList<>(contents.stacks.size());
            this.boundItems = new EnumMap<>(EquipmentSlot.class);
            this.selected = contents.selected;
            for (ItemStackTemplate stored : contents.stacks) {
                this.stacks.add(stored.create());
            }
            contents.boundEquipment.forEach((slot, template) -> this.boundItems.put(slot, template.create()));
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
            int count = Math.min(stack.getCount(), current.getMaxStackSize() - current.getCount());
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
            if (this.stacks.isEmpty() && this.boundItems.isEmpty()) {
                return DEFAULT;
            }
            ImmutableList.Builder<ItemStackTemplate> contentsBuilder = ImmutableList.builderWithExpectedSize(this.stacks.size());
            for (ItemStack stack : this.stacks) {
                contentsBuilder.add(ItemStackTemplate.fromNonEmptyStack(stack));
            }
            EnumMap<EquipmentSlot, ItemStackTemplate> boundEquipment = new EnumMap<>(EquipmentSlot.class);
            this.boundItems.forEach((slot, stack) -> boundEquipment.put(slot, ItemStackTemplate.fromNonEmptyStack(stack)));
            return new ArmourBundleContentsComponent(
              contentsBuilder.build(),
              boundEquipment,
              this.selected
            );
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
                if (ItemStack.isSameItemSameComponents(stack, this.stacks.get(i)))
                    return i;
            }
            return -1;
        }
    }
}
