package com.auroali.armourbundles.common.network;

import com.auroali.armourbundles.ArmourBundles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CycleEquippedC2S(boolean useNext) implements CustomPayload {
    public static PacketCodec<PacketByteBuf, CycleEquippedC2S> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, CycleEquippedC2S::useNext, CycleEquippedC2S::new);
    public static final CustomPayload.Id<CycleEquippedC2S> ID = new Id<>(ArmourBundles.id("swap_armor"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
