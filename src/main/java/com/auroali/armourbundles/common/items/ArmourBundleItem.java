package com.auroali.armourbundles.common.items;

import com.auroali.armourbundles.ArmourBundles;
import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class ArmourBundleItem extends Item {
    private final Identifier openBack;
    private final Identifier openFront;

    public ArmourBundleItem(Identifier openBack, Identifier openFront, Item.Settings settings) {
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
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return false;

        ArmourBundleContentsComponent.Builder builder = component.builder();
        ItemStack other = slot.getStack();
        if (clickType == ClickType.LEFT && !other.isEmpty()) {
            if (builder.add(other) == 0) {
                playInsertFailSound(player);
                return true;
            }

            playInsertSound(player);
            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        if (clickType == ClickType.RIGHT && other.isEmpty()) {
            ItemStack removed = builder.removeSelected();
            if (removed.isEmpty())
                return false;

            ItemStack remaining = slot.insertStack(removed);
            if (!remaining.isEmpty())
                builder.add(remaining);

            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
            setSelectedStack(stack, -1);
            return false;
        }

        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return false;

        ArmourBundleContentsComponent.Builder builder = component.builder();
        if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
            if (!slot.canTakePartial(player) || builder.add(otherStack) == 0) {
                playInsertFailSound(player);
                return true;
            }

            playInsertSound(player);
            stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }

        if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
            if (!slot.canTakePartial(player))
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
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null)
            return ActionResult.PASS;

        // try to equip items if not crouching
        if (!user.isSneaking()) {
            equipBundleItems(user, stack);
            return ActionResult.SUCCESS_SERVER;
        }

        // otherwise, insert them into the bundle
        ArmourBundleContentsComponent.Builder builder = component.builder();
        for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
            ItemStack equipped = user.getEquippedStack(slot);
            if (builder.add(equipped) > 0)
                playInsertSound(user);
        }

        builder.setSelected(-1);
        builder.clearBindings();
        stack.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
        return ActionResult.SUCCESS_SERVER;
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
                if (builder.hasBinding(slot) || !entity.canEquip(stack, slot))
                    continue;

                equipped = true;
                builder.bindItem(slot, stack);
                entity.equipStack(slot, stack);
                break;
            }

            if (!equipped)
                builder.add(stack);
        }

        bundle.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
    }

    public static boolean matchesEquipped(LivingEntity entity, ItemStack bundle) {
        ArmourBundleContentsComponent component = bundle.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        if (component == null || component.getBoundEquipment().isEmpty())
            return false;

        for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
            ItemStack equipped = entity.getEquippedStack(slot);
            ItemStack bound = component.getBoundEquipment().getOrDefault(slot, ItemStack.EMPTY);
            if (!ItemStack.areEqual(equipped, bound))
                return false;
        }
        return true;
    }

    public static int getEquippedBundleIndex(PlayerEntity entity) {
        for (int i = 0; i < entity.getInventory().main.size(); i++) {
            ItemStack stack = entity.getInventory().main.get(i);
            if (stack.contains(ArmourBundles.ARMOUR_BUNDLE_CONTENTS) && matchesEquipped(entity, stack))
                return i;
        }
        return -1;
    }

    public static ItemStack getNextArmourBundle(PlayerEntity entity, int index) {
        int start = index == -1 ? 0 : index;
        for (int i = start + 1; i < entity.getInventory().main.size(); i++) {
            ItemStack stack = entity.getInventory().main.get(i);
            if (stack.contains(ArmourBundles.ARMOUR_BUNDLE_CONTENTS) && !stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS).isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getPreviousArmourBundle(PlayerEntity entity, int index) {
        int start = index == -1 ? entity.getInventory().main.size() - 1 : index;
        for (int i = start - 1; i >= 0; i--) {
            ItemStack stack = entity.getInventory().main.get(i);
            if (stack.contains(ArmourBundles.ARMOUR_BUNDLE_CONTENTS) && !stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS).isEmpty())
                return stack;
        }
        return ItemStack.EMPTY;
    }

    private static void placeItemsIntoBundleOrInventory(LivingEntity entity) {
        if (!(entity.getWorld() instanceof ServerWorld world))
            return;

        if (entity instanceof PlayerEntity player) {
            PlayerInventory inventory = player.getInventory();
            ItemStack bundle = ItemStack.EMPTY;
            for (ItemStack stack : inventory.main) {
                if (!matchesEquipped(entity, stack))
                    continue;
                bundle = stack;
                break;
            }

            if (bundle.isEmpty()) {
                for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
                    ItemStack equipped = player.getEquippedStack(slot);
                    if (!equipped.isEmpty() && !player.getInventory().insertStack(equipped))
                        player.dropItem(equipped, true);
                }
                return;
            }

            ArmourBundleContentsComponent component = bundle.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
            ArmourBundleContentsComponent.Builder builder = component.builder();
            for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
                ItemStack stack = player.getEquippedStack(slot);
                if (stack.isEmpty())
                    continue;
                player.equipStack(slot, ItemStack.EMPTY);
                builder.add(stack);
                if (!stack.isEmpty() && !inventory.insertStack(stack))
                    player.dropItem(stack, true);
            }
            builder.setSelected(-1);
            builder.clearBindings();
            bundle.set(ArmourBundles.ARMOUR_BUNDLE_CONTENTS, builder.build());
        }

        for (EquipmentSlot slot : ArmourBundleContentsComponent.VALID_SLOTS) {
            ItemStack stack = entity.getEquippedStack(slot);
            entity.dropStack(world, stack);
            entity.equipStack(slot, ItemStack.EMPTY);
        }
    }

    private void onContentChanged(PlayerEntity user) {
        ScreenHandler screenHandler = user.currentScreenHandler;
        if (screenHandler != null) {
            screenHandler.onContentChanged(user.getInventory());
        }
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        ArmourBundleContentsComponent component = stack.get(ArmourBundles.ARMOUR_BUNDLE_CONTENTS);
        return component == null ? super.getTooltipData(stack) : Optional.of(component);
    }

    public Identifier getOpenBack() {
        return this.openBack;
    }

    public Identifier getOpenFront() {
        return this.openFront;
    }

    // from the vanilla bundle
    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

}
