package com.auroali.armourbundles;

import com.auroali.armourbundles.items.ArmourBundle;
import com.auroali.armourbundles.items.ArmourBundleInventory;
import com.auroali.armourbundles.items.Profiles;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ArmourBundles implements ModInitializer {
	// todo: fix bundle doing the item changed anim, readd bundle contents view
	public static final String MODID = "armourbundles";

	public static final DataComponentType<ArmourBundleInventory> ARMOUR_BUNDLE_INVENTORY = DataComponentType
			.<ArmourBundleInventory>builder()
			.codec(ItemStack.CODEC.listOf().xmap(ArmourBundleInventory::new, ArmourBundleInventory::stacks))
			.packetCodec(ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(ArmourBundleInventory::new, ArmourBundleInventory::stacks))
			.cache()
			.build();
	public static final DataComponentType<Integer> CURRENT_PROFILE = DataComponentType
			.<Integer>builder()
			.codec(Codec.INT)
			.packetCodec(PacketCodecs.VAR_INT)
			.build();

	public static final DataComponentType<Profiles> PROFILES = DataComponentType
			.<Profiles>builder()
			.codec(ArmourProfile.CODEC.listOf().xmap(Profiles::new, Profiles::profiles))
			.cache()
			.build();

	public static final ArmourBundle ARMOUR_BUNDLE = new ArmourBundle(new Item.Settings()
			.fireproof()
			.maxCount(1)
			.component(ARMOUR_BUNDLE_INVENTORY, ArmourBundleInventory.create())
			.component(CURRENT_PROFILE, 0)
			.component(PROFILES, Profiles.create(ArmourBundle.PROFILES))
			.rarity(Rarity.UNCOMMON)
	);

	public static final TagKey<Item> VALID_ARMOUR_BUNDLE_ITEMS = TagKey.of(RegistryKeys.ITEM, id("armor_bundle_insertable"));

	@Override
	public void onInitialize() {
		Registry.register(Registries.DATA_COMPONENT_TYPE, id("armour_bundle_inventory"), ARMOUR_BUNDLE_INVENTORY);
		Registry.register(Registries.DATA_COMPONENT_TYPE, id("current_profile"), CURRENT_PROFILE);
		Registry.register(Registries.DATA_COMPONENT_TYPE, id("profiles"), PROFILES);
		Registry.register(Registries.ITEM, id("armour_bundle"), ARMOUR_BUNDLE);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
						.register(content -> {
							content.add(ARMOUR_BUNDLE);
						});

		PayloadTypeRegistry.playC2S().register(EquipSlotC2SPacket.ID, EquipSlotC2SPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(EquipSlotC2SPacket.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			if(player.getItemCooldownManager().isCoolingDown(ARMOUR_BUNDLE))
				return;

			ItemStack armourBundle = ArmourBundle.findInInv(player);
			if(armourBundle.isEmpty())
				return;

			ARMOUR_BUNDLE.getProfile(payload.slot(), armourBundle)
					.ifPresent(profile -> ARMOUR_BUNDLE.tryEquip(armourBundle, player, profile));
		});
	}

	public static Identifier id(String id) {
		return new Identifier(MODID, id);
	}
}