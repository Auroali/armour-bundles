package com.auroali.armourbundles.items;

import com.auroali.armourbundles.ArmourBundles;
import com.auroali.armourbundles.ArmourProfile;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ArmourBundle extends Item {
    // about 3 full sets of armour, might change later
    public static final int MAX_SIZE = 12;
    public static final int PROFILES = 3;
    public static final int COOLDOWN_TICKS = 80;
    public ArmourBundle(Settings settings) {
        super(settings);
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
        NbtCompound itemNbt = bundle.getOrCreateNbt();
        NbtList inventory;
        if(!itemNbt.contains("Inv"))
            inventory = new NbtList();
        else inventory = itemNbt.getList("Inv", NbtElement.COMPOUND_TYPE);

        if(!canItemBeInserted(inventory, stack))
            return false;

        inventory.add(stack.writeNbt(new NbtCompound()));
        itemNbt.put("Inv", inventory);
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if(user.isSneaking()) {
            NbtCompound itemNbt = stack.getOrCreateNbt();
            int currentProfile = (itemNbt.getInt("CurrentProfile") + 1) % PROFILES;
            itemNbt.putInt("CurrentProfile", currentProfile);
            user.sendMessage(Text.translatable("item.armourprofiles.armour_bundle.profile_selected", currentProfile + 1), true);
            return TypedActionResult.success(stack, world.isClient);
        }
        setProfile(stack, user);
        user.sendMessage(Text.translatable("item.armourprofiles.armour_bundle.profile_set", stack.getOrCreateNbt().getInt("CurrentProfile") + 1), true);
        return TypedActionResult.success(stack, world.isClient);
    }

    public Optional<ItemStack> removeLastItem(ItemStack bundle) {
        NbtCompound itemNbt = bundle.getOrCreateNbt();
        NbtList inventory;
        if(!itemNbt.contains("Inv"))
            return Optional.empty();
        inventory = itemNbt.getList("Inv", NbtElement.COMPOUND_TYPE);
        if(inventory.isEmpty())
            return Optional.empty();

        final int indexToRemove = 0;
        // pull the stack from the inventory
        ItemStack stack = ItemStack.fromNbt(inventory.getCompound(indexToRemove));
        inventory.remove(indexToRemove);
        // return the stack
        return Optional.of(stack);
    }

    public void setProfile(ItemStack bundle, PlayerEntity entity) {
        NbtCompound itemNbt = bundle.getOrCreateNbt();
        NbtList profilesNbt;
        if(!itemNbt.contains("Profiles")) {
            profilesNbt = new NbtList();
            // populate the list
            for(int i = 0; i < PROFILES; i++)
                profilesNbt.add(new ArmourProfile().writeToNbt(new NbtCompound()));
        }
        else profilesNbt = itemNbt.getList("Profiles", NbtElement.COMPOUND_TYPE);

        int currentProfile = itemNbt.getInt("CurrentProfile");

        ArmourProfile profile = ArmourProfile.generateFrom(entity);

        profilesNbt.set(currentProfile, profile.writeToNbt(new NbtCompound()));
        itemNbt.put("Profiles", profilesNbt);
    }

    public Optional<ArmourProfile> getProfile(int profileIdx, ItemStack bundle) {
        NbtCompound itemNbt = bundle.getOrCreateNbt();
        if(!itemNbt.contains("Profiles"))
            return Optional.empty();
        NbtList profilesNbt = itemNbt.getList("Profiles", NbtElement.COMPOUND_TYPE);

        ArmourProfile profile = ArmourProfile.fromNbt(profilesNbt.getCompound(profileIdx));

        return Optional.of(profile);
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

    public boolean canItemBeInserted(NbtList inv, ItemStack stack) {
        return getItemsInBundleInv(inv) < MAX_SIZE && stack.getItem() instanceof ArmorItem && !EnchantmentHelper.hasBindingCurse(stack);
    }

    public Iterable<ItemStack> getItemsInBundle(ItemStack bundle) {
        NbtCompound itemNbt = bundle.getOrCreateNbt();
        NbtList inv = itemNbt.getList("Inv", NbtElement.COMPOUND_TYPE);

        return inv.stream()
                .map(NbtCompound.class::cast)
                .map(ItemStack::fromNbt)
                .toList();
    }

    public void removeStack(ItemStack bundle, ItemStack stackToRemove) {
        NbtCompound itemNbt = bundle.getOrCreateNbt();
        NbtList inv = itemNbt.getList("Inv", NbtElement.COMPOUND_TYPE);

        for(int i = 0; i < inv.size(); i++) {
            ItemStack comp = ItemStack.fromNbt((NbtCompound)inv.get(i));
            if(!comp.isItemEqual(stackToRemove) )
                continue;
            inv.remove(i);
            break;
        }

        itemNbt.put("Inv", inv);
    }

    public int getItemsInBundleInv(NbtList inv) {

        // probably not the fastest but thats ok, it won't be called nearly often enough
        // or on a large enough scale for that to be an issue
        return inv.stream()
                .map(NbtCompound.class::cast)
                .mapToInt(item -> item.getInt("Count"))
                .sum();
    }

    // from minecraft's bundle impl
    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.method_48926().getRandom().nextFloat() * 0.4F);
    }
    // from minecraft's bundle impl
    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.method_48926().getRandom().nextFloat() * 0.4F);
    }

    public static ItemStack findInInv(PlayerEntity player) {
        for(ItemStack stack : player.getInventory().main) {
            if(stack.isOf(ArmourBundles.ARMOUR_BUNDLE))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        int currentProfile = stack.getOrCreateNbt().getInt("CurrentProfile");
        tooltip.add(Text.of("%d/%d".formatted(currentProfile + 1, PROFILES)));
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.of();
        getItemsInBundle(stack).forEach(defaultedList::add);
        return Optional.of(new BundleTooltipData(defaultedList, 0));
    }
}
