package com.auroali.armourbundles.client;

import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

// basically just BundleTooltipComponent but for Armour Bundles
public class ArmourBundleContentsTooltipComponent implements ClientTooltipComponent {
    private static final Identifier BUNDLE_PROGRESS_BAR_BORDER_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_border");
    private static final Identifier BUNDLE_PROGRESS_BAR_FILL_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
    private static final Identifier BUNDLE_PROGRESS_BAR_FULL_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_full");
    private static final Identifier BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
    private static final Identifier BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");
    private static final Identifier BUNDLE_SLOT_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("container/bundle/slot_background");
    private static final Component BUNDLE_FULL = Component.translatable("item.minecraft.bundle.full");
    private static final Component BUNDLE_EMPTY = Component.translatable("item.minecraft.bundle.empty");
    private static final Component BUNDLE_EMPTY_DESCRIPTION = Component.translatable("item.armourbundles.armourbundle.empty.description");
    private static final Component BUNDLE_BOUND_ITEMS_DESCRIPTION = Component.translatable("item.armourbundles.armourbundle.bound.description");

    protected final ArmourBundleContentsComponent contents;

    public ArmourBundleContentsTooltipComponent(ArmourBundleContentsComponent contents) {
        this.contents = contents;
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, int width, int height, GuiGraphics context) {
        if (this.contents.isEmpty()) {
            this.drawEmpty(textRenderer, x, y, width, height, context);
            return;
        }

        int xOffset = x + this.getXMargin(width) + 96;

        for (int i = 1; i <= 4; i++) {
            int xPos = xOffset - i * 24;
            if (this.contents.getStacks().size() >= i)
                this.drawItem(textRenderer, context, xPos, y, i);
        }

        this.renderSelectedTooltip(textRenderer, context, x, y, width);
        this.drawProgressBar(textRenderer, context, x + this.getXMargin(width), y + this.getRowsHeight() + 4);
        this.drawBoundItems(context, textRenderer, x + this.getXMargin(width), y + this.getRowsHeight() + 17);
    }

    private void renderSelectedTooltip(Font renderer, GuiGraphics context, int x, int y, int width) {
        if (!this.contents.hasSelected())
            return;

        ItemStack selected = this.contents.getSelectedStack();
        Component name = selected.getStyledHoverName();
        int textWidth = renderer.width(name.getVisualOrderText());
        int xPos = x + width / 2 - 12;
        ClientTooltipComponent component = ClientTooltipComponent.create(name.getVisualOrderText());
        context.renderTooltip(
          renderer,
          List.of(component),
          xPos - textWidth / 2,
          y - 15,
          DefaultTooltipPositioner.INSTANCE,
          selected.get(DataComponents.TOOLTIP_STYLE)
        );
    }

    private void drawBoundItems(GuiGraphics context, Font textRenderer, int x, int y) {
        if (this.contents.getBoundEquipment().isEmpty())
            return;

        context.drawWordWrap(textRenderer, BUNDLE_BOUND_ITEMS_DESCRIPTION, x, y + 2, 96, 16777215);

        int index = 0;
        for (ItemStack stack : this.contents.getBoundEquipment().values()) {
            context.renderItem(stack, x + 4 + index * 24, y + this.getBoundDescriptionHeight(textRenderer) + 6);
            index++;
        }
    }

    private void drawItem(Font renderer, GuiGraphics context, int x, int y, int index) {
        int stackIndex = this.contents.getStacks().size() - index;
        ItemStack stack = this.contents.getStacks().get(stackIndex);
        if (stackIndex == this.contents.getSelected())
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE, x, y, 24, 24);
        else context.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x, y, 24, 24);

        context.renderItem(stack, x + 4, y + 4, index);
        context.renderItemDecorations(renderer, stack, x + 4, y + 4);
        if (stackIndex == this.contents.getSelected())
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE, x, y, 24, 24);
    }

    private void drawProgressBar(Font renderer, GuiGraphics context, int x, int y) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, this.getProgressBarTexture(), x + 1, y, this.getProgressBarWidth(), 13);
        context.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE, x, y, 96, 13);
        if (this.contents.getStacks().isEmpty()) {
            context.drawCenteredString(renderer, BUNDLE_EMPTY, x + 48, y + 3, 16777215);
        }

        if (this.contents.getStacks().size() >= ArmourBundleContentsComponent.MAX_STACKS) {
            context.drawCenteredString(renderer, BUNDLE_FULL, x + 48, y + 3, 16777215);
        }
    }

    private Identifier getProgressBarTexture() {
        return this.contents.getStacks().size() >= ArmourBundleContentsComponent.MAX_STACKS ? BUNDLE_PROGRESS_BAR_FULL_TEXTURE : BUNDLE_PROGRESS_BAR_FILL_TEXTURE;
    }

    private int getProgressBarWidth() {
        return (int) Math.clamp(94 * (this.contents.getStacks().size() / (double) ArmourBundleContentsComponent.MAX_STACKS), 0, 94);
    }

    public void drawEmpty(Font renderer, int x, int y, int width, int height, GuiGraphics context) {
        context.drawWordWrap(renderer, BUNDLE_EMPTY_DESCRIPTION, x + this.getXMargin(width), y, 96, 0xaaaaaa);
        this.drawProgressBar(renderer, context, x + this.getXMargin(width), y + this.getEmptyHeight(renderer) + 4);
        this.drawBoundItems(context, renderer, x + this.getXMargin(width), y + this.getEmptyHeight(renderer) + 17);
    }

    protected int getEmptyHeight(Font renderer) {
        return renderer.wordWrapHeight(BUNDLE_EMPTY_DESCRIPTION, 96);
    }

    public int getXMargin(int width) {
        return (width - 96) / 2;
    }

    public int getRowsHeight() {
        return this.getRows() * 24;
    }

    public int getRows() {
        return 1;
    }

    public int getBoundRowHeight(Font textRenderer) {
        return !this.contents.getBoundEquipment().isEmpty() ? getBoundDescriptionHeight(textRenderer) + 24 : 0;
    }

    public int getBoundDescriptionHeight(Font textRenderer) {
        return textRenderer.wordWrapHeight(BUNDLE_BOUND_ITEMS_DESCRIPTION, 96);
    }

    @Override
    public int getHeight(Font textRenderer) {
        return this.contents.isEmpty() ? this.getEmptyHeight(textRenderer) + this.getBoundRowHeight(textRenderer) + 21 : this.getRowsHeight() + this.getBoundRowHeight(textRenderer) + 21;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return 96;
    }
}
