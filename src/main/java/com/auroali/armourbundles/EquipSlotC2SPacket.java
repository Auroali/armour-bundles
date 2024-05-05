package com.auroali.armourbundles;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record EquipSlotC2SPacket(int slot) implements CustomPayload {
    public static PacketCodec<PacketByteBuf, EquipSlotC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, EquipSlotC2SPacket::slot, EquipSlotC2SPacket::new);
    public static final CustomPayload.Id<EquipSlotC2SPacket> ID = new Id<>(ArmourBundles.id("key_pressed"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
