package com.auroali.armourbundles.common.network;

import com.auroali.armourbundles.ArmourBundles;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CycleEquippedC2S(boolean useNext) implements CustomPacketPayload {
    public static StreamCodec<FriendlyByteBuf, CycleEquippedC2S> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, CycleEquippedC2S::useNext, CycleEquippedC2S::new);
    public static final CustomPacketPayload.Type<CycleEquippedC2S> ID = new Type<>(ArmourBundles.id("swap_armor"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
