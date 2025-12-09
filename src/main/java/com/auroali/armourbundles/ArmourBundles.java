package com.auroali.armourbundles;

import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;
import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.auroali.armourbundles.common.network.ArmourBundleScrollC2S;
import com.auroali.armourbundles.common.network.CycleEquippedC2S;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ArmourBundles implements ModInitializer {
    public static final String MODID = "armourbundles";

    public static final Identifier OPEN_BACK_TEXTURE = id("armour_bundle_open_back");
    public static final Identifier OPEN_FRONT_TEXTURE = id("armour_bundle_open_front");

    public static final DataComponentType<ArmourBundleContentsComponent> ARMOUR_BUNDLE_CONTENTS = DataComponentType
      .<ArmourBundleContentsComponent>builder()
      .persistent(ArmourBundleContentsComponent.CODEC)
      .networkSynchronized(ArmourBundleContentsComponent.PACKET_CODEC)
      .cacheEncoding()
      .build();

    public static final ResourceKey<Item> ARMOUR_BUNDLE_KEY = ResourceKey.create(Registries.ITEM, id("armour_bundle"));
    public static final ArmourBundleItem ARMOUR_BUNDLE = new ArmourBundleItem(OPEN_BACK_TEXTURE, OPEN_FRONT_TEXTURE, new Item.Properties()
      .fireResistant()
      .stacksTo(1)
      .component(ARMOUR_BUNDLE_CONTENTS, ArmourBundleContentsComponent.DEFAULT)
      .setId(ARMOUR_BUNDLE_KEY)
      .rarity(Rarity.UNCOMMON)
    );

    public static final TagKey<Item> VALID_ARMOUR_BUNDLE_ITEMS = TagKey.create(Registries.ITEM, id("armor_bundle_insertable"));

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("armour_bundle_contents"), ARMOUR_BUNDLE_CONTENTS);
        Registry.register(BuiltInRegistries.ITEM, ARMOUR_BUNDLE_KEY, ARMOUR_BUNDLE);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
          .register(content -> {
              content.accept(ARMOUR_BUNDLE);
          });

        PayloadTypeRegistry.playC2S().register(CycleEquippedC2S.ID, CycleEquippedC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(ArmourBundleScrollC2S.ID, ArmourBundleScrollC2S.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ArmourBundleScrollC2S.ID, (packet, ctx) -> {
            AbstractContainerMenu handler = ctx.player().containerMenu;
            if (handler == null)
                return;

            if (packet.slot() < 0 || packet.slot() >= handler.slots.size())
                return;
            Slot slot = handler.getSlot(packet.slot());
            ArmourBundleItem.setSelectedStack(slot.getItem(), packet.selected());
        });

        ServerPlayNetworking.registerGlobalReceiver(CycleEquippedC2S.ID, (packet, ctx) -> {
            int index = ArmourBundleItem.getEquippedBundleIndex(ctx.player());
            ItemStack bundle = packet.useNext() ? ArmourBundleItem.getNextArmourBundle(ctx.player(), index) : ArmourBundleItem.getPreviousArmourBundle(ctx.player(), index);
            if (bundle.isEmpty())
                return;

            ArmourBundleItem.equipBundleItems(ctx.player(), bundle);
        });
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MODID, id);
    }
}