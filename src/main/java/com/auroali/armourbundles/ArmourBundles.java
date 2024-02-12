package com.auroali.armourbundles;

import com.auroali.armourbundles.items.ArmourBundle;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class ArmourBundles implements ModInitializer {
	public static final String MODID = "armourbundles";

	public static final Identifier CHANNEL_ID = ArmourBundles.id("profile_key_listener");

	public static final ArmourBundle ARMOUR_BUNDLE = new ArmourBundle(new FabricItemSettings()
			.fireproof()
			.maxCount(1)
			.rarity(Rarity.UNCOMMON)
			.group(ItemGroup.COMBAT)
	);
	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, id("armour_bundle"), ARMOUR_BUNDLE);

		ServerPlayNetworking.registerGlobalReceiver(CHANNEL_ID, (server, player, handler, buf, responseSender) -> {
			int slot = buf.readByte();
			server.execute(() -> {
				if(player.getItemCooldownManager().isCoolingDown(ARMOUR_BUNDLE))
					return;

				ItemStack armourBundle = ArmourBundle.findInInv(player);
				if(armourBundle.isEmpty())
					return;

				ARMOUR_BUNDLE.getProfile(slot, armourBundle)
						.ifPresent(profile -> ARMOUR_BUNDLE.tryEquip(armourBundle, player, profile));
			});
		});
	}

	public static Identifier id(String id) {
		return new Identifier(MODID, id);
	}
}