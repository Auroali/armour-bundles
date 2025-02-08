package com.auroali.armourbundles.mixin.client;

import com.auroali.armourbundles.common.items.ArmourBundleItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @Shadow
    @Final
    private VertexConsumerProvider.Immediate vertexConsumers;

    @WrapOperation(
      method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
    )
    public void armourbundles$handleInventoryRender(ItemRenderer instance, ItemStack stack, ModelTransformationMode transformationMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, Operation<Void> original, @Local(argsOnly = true) World world, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true, ordinal = 2) int seed) {
        if (stack.getItem() instanceof ArmourBundleItem item && ArmourBundleItem.hasSelectedStack(stack)) {
            // get models
            boolean useInventoryModel = ItemRendererAccessor.armourbundles$shouldUseInventoryModel(transformationMode);
            BakedModel openBackModel = ((ItemRendererAccessor) instance).armourbundles$getModelOrOverride(((ItemRendererAccessor) instance).armourbundles$getModels().getModel(item.getOpenBack()), stack, world, entity, seed);
            BakedModel openFrontModel = ((ItemRendererAccessor) instance).armourbundles$getModelOrOverride(((ItemRendererAccessor) instance).armourbundles$getModels().getModel(item.getOpenFront()), stack, world, entity, seed);
            // render back
            ((ItemRendererZAccessor) instance).armourbundles$renderItemWithZ(stack, transformationMode, leftHanded, matrices, vertexConsumers, light, overlay, openBackModel, useInventoryModel, -1.5f);

            // render selected
            ItemStack selectedStack = ArmourBundleItem.getSelectedStack(stack);
            BakedModel selectedModel = instance.getModel(selectedStack, world, entity, seed);
            ((ItemRendererAccessor) instance).armourbundles$renderItem(selectedStack, transformationMode, leftHanded, matrices, vertexConsumers, light, overlay, selectedModel, useInventoryModel);

            // render front
            ((ItemRendererZAccessor) instance).armourbundles$renderItemWithZ(stack, transformationMode, leftHanded, matrices, vertexConsumers, light, overlay, openFrontModel, useInventoryModel, 0.5f);
            return;
        }
        original.call(instance, stack, transformationMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
    }
}
