package com.auroali.armourbundles.common.items;

import com.auroali.armourbundles.ArmourBundles;
import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;

import java.util.Optional;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArmourBundleItem extends Item {
    private final Identifier openBack;
    private final Identifier openFront;

    public ArmourBundleItem(Identifier openBack, Identifier openFront, Item.Properties settings) {
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
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return false;

        ArmourBundleContentsComponent.Builder builder = component.builder();
        ItemStack other = slot.getItem();
        if (clickType == ClickAction.PRIMARY && !other.isEmpty()) {
            if (builder.add(other) == 0) {
                playInsertFailSound(player);
                return true;
            }

            playInsertSound(player);
            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        if (clickType == ClickAction.SECONDARY && other.isEmpty()) {
            ItemStack removed = builder.removeSelected();
            if (removed.isEmpty())
                return false;

            ItemStack remaining = slot.safeInsert(removed);
            if (!remaining.isEmpty())
                builder.add(remaining);

            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        if (clickType == ClickAction.PRIMARY && otherStack.isEmpty()) {
            setSelectedStack(stack, -1);
            return false;
        }

        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return false;

        ArmourBundleContentsComponent.Builder builder = component.builder();
        if (clickType == ClickAction.PRIMARY && !otherStack.isEmpty()) {
            if (!slot.allowModification(player) || builder.add(otherStack) == 0) {
                playInsertFailSound(player);
                return true;
            }

            playInsertSound(player);
            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        if (clickType == ClickAction.SECONDARY && otherStack.isEmpty()) {
            if (!slot.allowModification(player))
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

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return InteractionResult.PASS;

        // try to equip items if not crouching
        if (!user.isShiftKeyDown()) {
            equipBundleItems(user, stack);
            return InteractionResult.SUCCESS_SERVER;
        }

        // otherwise, insert them into the bundle
        ArmourBundleContentsComponent.Builder builder = component.builder();
        for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
            ItemStack equipped = user.getItemBySlot(slot);
            if (builder.add(equipped) > 0)
                playInsertSound(user);
        }

        builder.setSelected(-1);
        builder.clearBindings();
        stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
        return InteractionResult.SUCCESS_SERVER;
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

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        return component == null ? super.getTooltipImage(stack) : Optional.of(component);
    }

    public Identifier getOpenBack() {
        return this.openBack;
    }

    public Identifier getOpenFront() {
        return this.openFront;
    }

    // from the vanilla bundle
    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

    private void onContentChanged(Player user) {
        AbstractContainerMenu screenHandler = user.containerMenu;
        if (screenHandler != null) {
            screenHandler.slotsChanged(user.getInventory());
        }
    }

    /**
     * Attempts to equip all items in the provided armour bundle
     *
     * @param entity the entity to equip items to
     * @param bundle the armour bundle to equip items from
     */
    public static void equipBundleItems(LivingEntity entity, ItemStack bundle) {
        placeItemsIntoBundleOrInventory(entity);
        ArmourBundleContentsComponent component = bundle.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return;


        ArmourBundleContentsComponent.Builder builder = component.builder();
        builder.setSelected(-1);
        builder.clearBindings();

        for (int i = 0; i < component.getStacks().size(); i++) {
            ItemStack stack = builder.removeSelected();
            if (stack.isEmpty())
                break;

            boolean equipped = false;
            for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
                if (builder.hasBinding(slot) || !entity.isEquippableInSlot(stack, slot))
                    continue;

                equipped = true;
                builder.bindItem(slot, stack);
                entity.setItemSlot(slot, stack);
                break;
            }

            if (!equipped)
                builder.add(stack);
        }

        bundle.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
    }

    /**
     * Check if the given armour bundle's bound items match the currently equipped items of the entity
     *
     * @param entity the entity to check
     * @param bundle the bundle to check
     * @return if the items match
     */
    public static boolean matchesEquipped(LivingEntity entity, ItemStack bundle) {
        ArmourBundleContentsComponent component = bundle.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null || component.getBoundEquipment().isEmpty())
            return false;

        for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
            ItemStack equipped = entity.getItemBySlot(slot);
            ItemStack bound = component.getBoundEquipment().getOrDefault(slot, ItemStack.EMPTY);
            if (!ItemStack.matchesIgnoringComponents(equipped, bound, ArmourBundleItem::shouldIgnoreForEquipmentComparision))
                return false;
        }
        return true;
    }

    /**
     * Gets the index of the armour bundle that matches all currently equipped items
     *
     * @param entity the entity to check
     * @return the index of the armour bundle, or -1 if one wasn't found
     */
    public static int getEquippedBundleIndex(Player entity) {
        for (int i = 0; i < entity.getInventory().getNonEquipmentItems().size(); i++) {
            ItemStack stack = entity.getInventory().getNonEquipmentItems().get(i);
            if (stack.has(ArmourBundles.ARMOUR_BUNDLE_CONTENTS) && matchesEquipped(entity, stack))
                return i;
        }
        return -1;
    }

    /**
     * Gets the next unequipped armour bundle in a player's inventory
     *
     * @param entity the player
     * @param index  the starting index
     * @return the next armour bundle, or empty if none was found
     */
    @Deprecated(forRemoval = true)
    public static ItemStack getNextArmourBundle(Player entity, int index) {
        int start = index == -1 ? 0 : index;
        for (int i = start + 1; i < entity.getInventory().getNonEquipmentItems().size(); i++) {
            ItemStack stack = entity.getInventory().getNonEquipmentItems().get(i);
            if (stack.has(ArmourBundles.ARMOUR_BUNDLE_CONTENTS) && !stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS).isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Gets the previous unequipped armour bundle in a player's inventory
     *
     * @param entity the player
     * @param index  the starting index
     * @return the previous armour bundle, or empty if none was found
     */
    @Deprecated(forRemoval = true)
    public static ItemStack getPreviousArmourBundle(Player entity, int index) {
        int start = index == -1 ? entity.getInventory().getNonEquipmentItems().size() - 1 : index;
        for (int i = start - 1; i >= 0; i--) {
            ItemStack stack = entity.getInventory().getNonEquipmentItems().get(i);
            if (stack.has(ArmourBundles.ARMOUR_BUNDLE_CONTENTS) && !stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS).isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Locates the next non-empty armour bundle in a player's inventory
     *
     * @param player        the player to search
     * @param startIndex    the index to start from
     * @param searchForward whether to search forward or backwards
     * @return the found item stack, or an empty one if nothing was found
     */
    public static ItemStack findNextBundle(Player player, int startIndex, boolean searchForward) {
        NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();
        if (startIndex == -1)
            startIndex = searchForward ? -1 : items.size();

        for (int i = 1; i < items.size(); i++) {
            int currentIndex;
            if (searchForward)
                currentIndex = (startIndex + i) % items.size();
            else {
                currentIndex = startIndex - i;
                if (currentIndex < 0)
                    currentIndex = items.size() + currentIndex;
            }
            ItemStack stack = items.get(currentIndex);
            if (stack.has(ArmourBundles.ARMOUR_BUNDLE_CONTENTS) && !stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS).isEmpty()) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static void placeItemsIntoBundleOrInventory(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel world))
            return;

        if (entity instanceof Player player) {
            Inventory inventory = player.getInventory();
            ItemStack bundle = ItemStack.EMPTY;
            for (ItemStack stack : inventory.getNonEquipmentItems()) {
                if (!matchesEquipped(entity, stack))
                    continue;
                bundle = stack;
                break;
            }

            if (bundle.isEmpty()) {
                for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
                    ItemStack equipped = player.getItemBySlot(slot);
                    if (!equipped.isEmpty() && !player.getInventory().add(equipped))
                        player.drop(equipped, true);
                }
                return;
            }

            ArmourBundleContentsComponent component = bundle.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
            ArmourBundleContentsComponent.Builder builder = component.builder();
            for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.isEmpty())
                    continue;
                player.setItemSlot(slot, ItemStack.EMPTY);
                builder.add(stack);
                if (!stack.isEmpty() && !inventory.add(stack))
                    player.drop(stack, true);
            }
            builder.setSelected(-1);
            builder.clearBindings();
            bundle.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
        }

        for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            entity.spawnAtLocation(world, stack);
            entity.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    private static boolean shouldIgnoreForEquipmentComparision(DataComponentType<?> type) {
        return type == DataComponents.DAMAGE;
    }
}
