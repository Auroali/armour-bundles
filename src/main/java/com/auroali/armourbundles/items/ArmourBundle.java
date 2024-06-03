package com.auroali.armourbundles.items;

import com.auroali.armourbundles.ArmourBundles;
import com.auroali.armourbundles.ArmourProfile;
import com.auroali.armourbundles.items.tooltipdata.ArmourBundleTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArmourBundle extends Item {
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);
    // about 3 full sets of armour, might change later
    public static final int MAX_SIZE = 12;
    public static final int PROFILES = 3;
    public static final int COOLDOWN_TICKS = 80;
    public ArmourBundle(Settings settings) {
        super(settings);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return super.allowComponentsUpdateAnimation(player, hand, oldStack, newStack);
    }

    public float getFillPercent(ItemStack stack) {
        return 0.0f; //(float) stack.get(ArmourBundles.ARMOUR_BUNDLE_INVENTORY).stacks().size() / MAX_SIZE;
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if(clickType != ClickType.RIGHT)
            return false;
        // if theres a stack in the clicked on slot, we try to insert it
        if(slot.hasStack() && tryInsert(stack, slot.getStack())) {
            slot.getStack().setCount(0);
            playInsertSound(player);
            return true;
        }
        // if there isn't, we try to remove an item and place it into the slot
        if(!slot.hasStack()) {
            removeLastItem(stack).ifPresent(slot::setStack);
            playRemoveOneSound(player);
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if(clickType != ClickType.RIGHT)
            return false;

        if(!otherStack.isEmpty() && tryInsert(stack, otherStack)) {
            otherStack.setCount(0);
            playInsertSound(player);
            return true;
        }

        if(otherStack.isEmpty()) {
            removeLastItem(stack).ifPresent(cursorStackReference::set);
            playRemoveOneSound(player);
            return true;
        }
        return false;
    }

    public boolean tryInsert(ItemStack bundle, ItemStack stack) {
        ArmourBundleInventory inventory = bundle.get(ArmourBundles.ARMOUR_BUNDLE_INVENTORY);
        if(inventory != null && canItemBeInserted(inventory, stack)) {
            bundle.set(ArmourBundles.ARMOUR_BUNDLE_INVENTORY, ArmourBundleInventory.create(inventory, stack.copy()));
            return true;
        }
            //        NbtCompound itemNbt = bundle.getOrCreateNbt();
//        NbtList inventory;
//        if(!itemNbt.contains("Inv"))
//            inventory = new NbtList();
//        else inventory = itemNbt.getList("Inv", NbtElement.COMPOUND_TYPE);
//
//        if(!canItemBeInserted(inventory, stack))
//            return false;
//
//        inventory.add(stack.writeNbt(new NbtCompound()));
//        itemNbt.put("Inv", inventory);
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if(user.isSneaking()) {

            int currentProfile = (stack.get(ArmourBundles.CURRENT_PROFILE) + 1) % PROFILES;
            stack.set(ArmourBundles.CURRENT_PROFILE, currentProfile);
            user.sendMessage(Text.translatable("item.armourprofiles.armour_bundle.profile_selected", currentProfile + 1), true);
            return TypedActionResult.success(stack, world.isClient);
        }
        setProfile(stack, user);
        user.sendMessage(Text.translatable("item.armourprofiles.armour_bundle.profile_set", stack.get(ArmourBundles.CURRENT_PROFILE) + 1), true);
        return TypedActionResult.success(stack, world.isClient);
    }

    public Optional<ItemStack> removeLastItem(ItemStack bundle) {
        ArmourBundleInventory inventory = bundle.get(ArmourBundles.ARMOUR_BUNDLE_INVENTORY);
        if(inventory == null || inventory.stacks().isEmpty())
            return Optional.empty();

        ItemStack stack = inventory.stacks().getLast();
        bundle.set(ArmourBundles.ARMOUR_BUNDLE_INVENTORY, inventory.remove(stack));
        return Optional.of(stack);
    }

    public void setProfile(ItemStack bundle, PlayerEntity entity) {
        Profiles profiles = bundle.get(ArmourBundles.PROFILES);
        int currentProfile = bundle.get(ArmourBundles.CURRENT_PROFILE);

        ArmourProfile profile = ArmourProfile.generateFrom(entity);
        bundle.set(ArmourBundles.PROFILES, profiles.with(currentProfile, profile));
    }

    public Optional<ArmourProfile> getProfile(int profileIdx, ItemStack bundle) {
        Profiles profiles = bundle.get(ArmourBundles.PROFILES);
        if(profileIdx >= profiles.profiles().size())
            return Optional.empty();
        return Optional.of(profiles.profiles().get(profileIdx));
    }

    // this is in here instead of ArmourProfile because it requires moving items between the bundle
    // and the player's inventory, which seemed better suited for this
    public void tryEquip(ItemStack bundle, PlayerEntity player, ArmourProfile profile) {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            if(slot.getType() != EquipmentSlot.Type.ARMOR)
                continue;

            ItemStack current = player.getEquippedStack(slot);

            if(profile.matches(slot, current))
                continue;

            if(profile.getItemInSlot(slot).isEmpty()) {
                if(!current.isEmpty() && tryInsert(bundle, current)) {
                    current.setCount(0);
                    playInsertSound(player);
                    continue;
                }
            }

            for(ItemStack stack : getItemsInBundle(bundle)) {
                if(!profile.matches(slot, stack))
                    continue;

                removeStack(bundle, stack);

                // so this is probably a terrible way to go about things
                // i just wanted the sound without the duplicated equip call ._.
                boolean flag = current.isEmpty();
                if(!flag && tryInsert(bundle, current)) {
                    flag = true;
                    playInsertSound(player);
                }
                if(flag) {
                    player.equipStack(slot, stack);
                    break;
                }
                else tryInsert(bundle, stack);

            }
        }

        player.getItemCooldownManager().set(bundle.getItem(), COOLDOWN_TICKS);
    }

    public boolean canItemBeInserted(ArmourBundleInventory inv, ItemStack stack) {
        return inv.stacks().size() < MAX_SIZE && (stack.getItem() instanceof ArmorItem || stack.isIn(ArmourBundles.VALID_ARMOUR_BUNDLE_ITEMS)) && !EnchantmentHelper.hasBindingCurse(stack);
    }

    public Iterable<ItemStack> getItemsInBundle(ItemStack bundle) {
        ArmourBundleInventory inventory = bundle.get(ArmourBundles.ARMOUR_BUNDLE_INVENTORY);
        if(inventory == null)
            return Collections.emptyList();
        return inventory.stacks();
    }

    public void removeStack(ItemStack bundle, ItemStack stackToRemove) {
        ArmourBundleInventory inv = bundle.get(ArmourBundles.ARMOUR_BUNDLE_INVENTORY);

        if(inv != null && inv.stacks().contains(stackToRemove))
            bundle.set(ArmourBundles.ARMOUR_BUNDLE_INVENTORY, inv.remove(stackToRemove));
    }

    // from minecraft's bundle impl
    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }
    // from minecraft's bundle impl
    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static ItemStack findInInv(PlayerEntity player) {
        for(ItemStack stack : player.getInventory().main) {
            if(stack.isOf(ArmourBundles.ARMOUR_BUNDLE))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        int profile = stack.getOrDefault(ArmourBundles.CURRENT_PROFILE, 0) + 1;
        tooltip.add(Text.translatable("item.armourprofiles.armour_bundle.current_profile", profile, PROFILES));
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {

        ArmourBundleInventory inventory = entity.getStack().get(ArmourBundles.ARMOUR_BUNDLE_INVENTORY);
        if (inventory != null) {
            entity.getStack().set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
            ItemUsage.spawnItemContents(entity, inventory.stacks());
        }
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return !stack.contains(DataComponentTypes.HIDE_TOOLTIP) && !stack.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP) ?
                Optional.ofNullable(stack.get(ArmourBundles.ARMOUR_BUNDLE_INVENTORY)).map(ArmourBundleTooltipData::new)
                : Optional.empty();
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        ArmourBundleInventory inventory = stack.getOrDefault(ArmourBundles.ARMOUR_BUNDLE_INVENTORY, ArmourBundleInventory.create());
        return !inventory.stacks().isEmpty();
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        ArmourBundleInventory inventory = stack.getOrDefault(ArmourBundles.ARMOUR_BUNDLE_INVENTORY, ArmourBundleInventory.create());
        return Math.min(1 + (int) (inventory.stacks().size() / (float) MAX_SIZE * 12), 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }
}
