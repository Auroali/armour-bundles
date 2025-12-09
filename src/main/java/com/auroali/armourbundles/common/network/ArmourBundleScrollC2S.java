package com.auroali.armourbundles.common.network;

import com.auroali.armourbundles.ArmourBundles;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ArmourBundleScrollC2S(int slot, int selected) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ArmourBundleScrollC2S> CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      ArmourBundleScrollC2S::slot,
      ByteBufCodecs.VAR_INT,
      ArmourBundleScrollC2S::selected,
      ArmourBundleScrollC2S::new
    );
    public static final CustomPacketPayload.Type<ArmourBundleScrollC2S> ID = new Type<>(ArmourBundles.id("scroll"));

    @Override
    public Type<ArmourBundleScrollC2S> type() {
        return ID;
    }
}
