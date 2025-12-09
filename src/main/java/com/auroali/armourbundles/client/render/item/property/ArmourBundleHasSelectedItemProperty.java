package com.auroali.armourbundles.client.render.item.property;

import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ArmourBundleHasSelectedItemProperty implements ConditionalItemModelProperty {
    public static final MapCodec<ArmourBundleHasSelectedItemProperty> CODEC = MapCodec.unit(new ArmourBundleHasSelectedItemProperty());

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return CODEC;
    }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return ArmourBundleItem.hasSelectedStack(stack);
    }
}
