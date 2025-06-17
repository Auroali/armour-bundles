package com.auroali.armourbundles.client;

import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

// basically just BundleTooltipComponent but for Armour Bundles
public class ArmourBundleContentsTooltipComponent implements TooltipComponent {
    private static final Identifier BUNDLE_PROGRESS_BAR_BORDER_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_border");
    private static final Identifier BUNDLE_PROGRESS_BAR_FILL_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_fill");
    private static final Identifier BUNDLE_PROGRESS_BAR_FULL_TEXTURE = Identifier.ofVanilla("container/bundle/bundle_progressbar_full");
    private static final Identifier BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.ofVanilla("container/bundle/slot_highlight_back");
    private static final Identifier BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.ofVanilla("container/bundle/slot_highlight_front");
    private static final Identifier BUNDLE_SLOT_BACKGROUND_TEXTURE = Identifier.ofVanilla("container/bundle/slot_background");
    private static final Text BUNDLE_FULL = Text.translatable("item.minecraft.bundle.full");
    private static final Text BUNDLE_EMPTY = Text.translatable("item.minecraft.bundle.empty");
    private static final Text BUNDLE_EMPTY_DESCRIPTION = Text.translatable("item.armourbundles.armourbundle.empty.description");
    private static final Text BUNDLE_BOUND_ITEMS_DESCRIPTION = Text.translatable("item.armourbundles.armourbundle.bound.description");

    protected final ArmourBundleContentsComponent contents;

    public ArmourBundleContentsTooltipComponent(ArmourBundleContentsComponent contents) {
        this.contents = contents;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
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

    private void renderSelectedTooltip(TextRenderer renderer, DrawContext context, int x, int y, int width) {
        if (!this.contents.hasSelected())
            return;

        ItemStack selected = this.contents.getSelectedStack();
        Text name = selected.getFormattedName();
        int textWidth = renderer.getWidth(name.asOrderedText());
        int xPos = x + width / 2 - 12;
        TooltipComponent component = TooltipComponent.of(name.asOrderedText());
        context.drawTooltipImmediately(
          renderer,
          List.of(component),
          xPos - textWidth / 2,
          y - 15,
          HoveredTooltipPositioner.INSTANCE,
          selected.get(DataComponentTypes.TOOLTIP_STYLE)
        );
    }

    private void drawBoundItems(DrawContext context, TextRenderer textRenderer, int x, int y) {
        if (this.contents.getBoundEquipment().isEmpty())
            return;

        context.drawWrappedTextWithShadow(textRenderer, BUNDLE_BOUND_ITEMS_DESCRIPTION, x, y + 2, 96, 16777215);

        int index = 0;
        for (ItemStack stack : this.contents.getBoundEquipment().values()) {
            context.drawItem(stack, x + 4 + index * 24, y + this.getBoundDescriptionHeight(textRenderer) + 6);
            index++;
        }
    }

    private void drawItem(TextRenderer renderer, DrawContext context, int x, int y, int index) {
        int stackIndex = this.contents.getStacks().size() - index;
        ItemStack stack = this.contents.getStacks().get(stackIndex);
        if (stackIndex == this.contents.getSelected())
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE, x, y, 24, 24);
        else context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x, y, 24, 24);

        context.drawItem(stack, x + 4, y + 4, index);
        context.drawStackOverlay(renderer, stack, x + 4, y + 4);
        if (stackIndex == this.contents.getSelected())
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE, x, y, 24, 24);
    }

    private void drawProgressBar(TextRenderer renderer, DrawContext context, int x, int y) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.getProgressBarTexture(), x + 1, y, this.getProgressBarWidth(), 13);
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE, x, y, 96, 13);
        if (this.contents.getStacks().isEmpty()) {
            context.drawCenteredTextWithShadow(renderer, BUNDLE_EMPTY, x + 48, y + 3, 16777215);
        }

        if (this.contents.getStacks().size() >= ArmourBundleContentsComponent.MAX_STACKS) {
            context.drawCenteredTextWithShadow(renderer, BUNDLE_FULL, x + 48, y + 3, 16777215);
        }
    }

    private Identifier getProgressBarTexture() {
        return this.contents.getStacks().size() >= ArmourBundleContentsComponent.MAX_STACKS ? BUNDLE_PROGRESS_BAR_FULL_TEXTURE : BUNDLE_PROGRESS_BAR_FILL_TEXTURE;
    }

    private int getProgressBarWidth() {
        return (int) Math.clamp(94 * (this.contents.getStacks().size() / (double) ArmourBundleContentsComponent.MAX_STACKS), 0, 94);
    }

    public void drawEmpty(TextRenderer renderer, int x, int y, int width, int height, DrawContext context) {
        context.drawWrappedTextWithShadow(renderer, BUNDLE_EMPTY_DESCRIPTION, x + this.getXMargin(width), y, 96, 0xaaaaaa);
        this.drawProgressBar(renderer, context, x + this.getXMargin(width), y + this.getEmptyHeight(renderer) + 4);
        this.drawBoundItems(context, renderer, x + this.getXMargin(width), y + this.getEmptyHeight(renderer) + 17);
    }

    protected int getEmptyHeight(TextRenderer renderer) {
        return renderer.getWrappedLinesHeight(BUNDLE_EMPTY_DESCRIPTION, 96);
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

    public int getBoundRowHeight(TextRenderer textRenderer) {
        return !this.contents.getBoundEquipment().isEmpty() ? getBoundDescriptionHeight(textRenderer) + 24 : 0;
    }

    public int getBoundDescriptionHeight(TextRenderer textRenderer) {
        return textRenderer.getWrappedLinesHeight(BUNDLE_BOUND_ITEMS_DESCRIPTION, 96);
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return this.contents.isEmpty() ? this.getEmptyHeight(textRenderer) + this.getBoundRowHeight(textRenderer) + 21 : this.getRowsHeight() + this.getBoundRowHeight(textRenderer) + 21;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 96;
    }
}
