package com.auroali.armourbundles.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {
    @Invoker("shouldUseInventoryModel")
    static boolean armourbundles$shouldUseInventoryModel(ModelTransformationMode mode) {
        throw new IllegalStateException("Mixin not applied");
    }

    @Invoker("getModelOrOverride")
    BakedModel armourbundles$getModelOrOverride(BakedModel model, ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed);

    @Invoker("renderItem")
    void armourbundles$renderItem(ItemStack stack, ModelTransformationMode transformationMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, boolean useInventoryModel);

    @Accessor("models")
    ItemModels armourbundles$getModels();
}
