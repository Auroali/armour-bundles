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
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;

public class ArmourBundleSelectedItemModel implements ItemModel {
    static final ArmourBundleSelectedItemModel INSTANCE = new ArmourBundleSelectedItemModel();

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel world, @Nullable ItemOwner heldItemContext, int seed) {
        state.appendModelIdentityElement(this);
        ItemStackTemplate inner = ArmourBundleItem.getSelectedStack(stack);
        if (inner != null)
            resolver.appendItemLayers(state, inner.create(), displayContext, world, heldItemContext, seed);
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked> CODEC = MapCodec.unit(new com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked());

        @Override
        public MapCodec<com.auroali.armourbundles.client.render.item.model.ArmourBundleSelectedItemModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public @NonNull ItemModel bake(@NonNull BakingContext context, @NonNull Matrix4fc transform) {
            return ArmourBundleSelectedItemModel.INSTANCE;
        }

        @Override
        public void resolveDependencies(@NonNull Resolver resolver) {

        }
    }
}
