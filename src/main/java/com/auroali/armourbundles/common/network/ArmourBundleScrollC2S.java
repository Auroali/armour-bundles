package com.auroali.armourbundles.common.network;

import com.auroali.armourbundles.ArmourBundles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ArmourBundleScrollC2S(int slot, int selected) implements CustomPayload {
    public static final PacketCodec<PacketByteBuf, ArmourBundleScrollC2S> CODEC = PacketCodec.tuple(
      PacketCodecs.VAR_INT,
      ArmourBundleScrollC2S::slot,
      PacketCodecs.VAR_INT,
      ArmourBundleScrollC2S::selected,
      ArmourBundleScrollC2S::new
    );
    public static final CustomPayload.Id<ArmourBundleScrollC2S> ID = new Id<>(ArmourBundles.id("scroll"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
