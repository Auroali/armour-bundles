package com.auroali.armourbundles;

import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.auroali.armourbundles.common.items.components.ArmourBundleContentsComponent;
import com.auroali.armourbundles.common.network.ArmourBundleScrollC2S;
import com.auroali.armourbundles.common.network.EquipSlotC2SPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ArmourBundles implements ModInitializer {
    public static final String MODID = "armourbundles";

    public static final Identifier OPEN_BACK_TEXTURE = id("armour_bundle_open_back");
    public static final Identifier OPEN_FRONT_TEXTURE = id("armour_bundle_open_front");

    public static final ComponentType<ArmourBundleContentsComponent> ARMOUR_BUNDLE_CONTENTS = ComponentType
      .<ArmourBundleContentsComponent>builder()
      .codec(ArmourBundleContentsComponent.CODEC)
      .packetCodec(ArmourBundleContentsComponent.PACKET_CODEC)
      .cache()
      .build();

    public static final RegistryKey<Item> ARMOUR_BUNDLE_KEY = RegistryKey.of(RegistryKeys.ITEM, id("armour_bundle"));
    public static final ArmourBundleItem ARMOUR_BUNDLE = new ArmourBundleItem(OPEN_BACK_TEXTURE, OPEN_FRONT_TEXTURE, new Item.Settings()
      .fireproof()
      .maxCount(1)
      .component(ARMOUR_BUNDLE_CONTENTS, ArmourBundleContentsComponent.DEFAULT)
      .registryKey(ARMOUR_BUNDLE_KEY)
      .rarity(Rarity.UNCOMMON)
    );

    public static final TagKey<Item> VALID_ARMOUR_BUNDLE_ITEMS = TagKey.of(RegistryKeys.ITEM, id("armor_bundle_insertable"));

    @Override
    public void onInitialize() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, id("armour_bundle_inventory"), ARMOUR_BUNDLE_CONTENTS);
        Registry.register(Registries.ITEM, ARMOUR_BUNDLE_KEY, ARMOUR_BUNDLE);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
          .register(content -> {
              content.add(ARMOUR_BUNDLE);
          });

        PayloadTypeRegistry.playC2S().register(EquipSlotC2SPacket.ID, EquipSlotC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ArmourBundleScrollC2S.ID, ArmourBundleScrollC2S.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ArmourBundleScrollC2S.ID, (packet, ctx) -> {
            ScreenHandler handler = ctx.player().currentScreenHandler;
            if (handler == null)
                return;

            if (packet.slot() < 0 || packet.slot() >= handler.slots.size())
                return;
            Slot slot = handler.getSlot(packet.slot());
            ArmourBundleItem.setSelectedStack(slot.getStack(), packet.selected());
        });
    }

    public static Identifier id(String id) {
        return Identifier.of(MODID, id);
    }
}