package com.auroali.armourbundles.client.render.item.model;

import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import org.jetbrains.annotations.Nullable;

public class ArmourBundleSelectedItemModel implements ItemModel {
    static final ArmourBundleSelectedItemModel INSTANCE = new ArmourBundleSelectedItemModel();

    @Override
    public void update(ItemRenderState state, ItemStack stack, ItemModelManager resolver, ItemDisplayContext displayContext, @Nullable ClientWorld world, @Nullable HeldItemContext heldItemContext, int seed) {
        ItemStack inner = ArmourBundleItem.getSelectedStack(stack);
        if (!inner.isEmpty())
            resolver.update(state, inner, displayContext, world, heldItemContext, seed);
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked> CODEC = MapCodec.unit(new com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked());

        @Override
        public MapCodec<com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public ItemModel bake(BakeContext context) {
            return ArmourBundleSelectedItemModel.INSTANCE;
        }

        @Override
        public void resolve(Resolver resolver) {

        }
    }
}
