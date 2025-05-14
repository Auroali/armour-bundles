package com.auroali.armourbundles.client.render.item.property;

import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ArmourBundleHasSelectedItemProperty implements BooleanProperty {
    public static final MapCodec<ArmourBundleHasSelectedItemProperty> CODEC = MapCodec.unit(new ArmourBundleHasSelectedItemProperty());

    @Override
    public MapCodec<? extends BooleanProperty> getCodec() {
        return CODEC;
    }

    @Override
    public boolean test(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return ArmourBundleItem.hasSelectedStack(stack);
    }
}
