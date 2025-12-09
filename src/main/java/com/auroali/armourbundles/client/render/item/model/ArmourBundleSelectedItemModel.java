package com.auroali.armourbundles.client.render.item.model;

import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ArmourBundleSelectedItemModel implements ItemModel {
    static final ArmourBundleSelectedItemModel INSTANCE = new ArmourBundleSelectedItemModel();

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel world, @Nullable ItemOwner heldItemContext, int seed) {
        ItemStack inner = ArmourBundleItem.getSelectedStack(stack);
        if (!inner.isEmpty())
            resolver.appendItemLayers(state, inner, displayContext, world, heldItemContext, seed);
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked> CODEC = MapCodec.unit(new com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked());

        @Override
        public MapCodec<com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public ItemModel bake(BakingContext context) {
            return ArmourBundleSelectedItemModel.INSTANCE;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {

        }
    }
}
