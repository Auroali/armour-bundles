package com.auroali.armourbundles.client;

import com.auroali.armourbundles.common.components.ArmourBundleContentsComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.NonNull;

import java.util.List;

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
    public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
        if (this.contents.isEmpty()) {
            this.extractEmpty(font, x, y, w, graphics);
        } else {
            this.extractNonEmpty(font, x, y, w, graphics);
        }
    }

    private void extractEmpty(Font textRenderer, int x, int y, int width, GuiGraphicsExtractor graphics) {
        int left = x + getXMargin(width);
        graphics.textWithWordWrap(textRenderer, BUNDLE_EMPTY_DESCRIPTION, left, y, 96, 0xffaaaaaa);
        this.extractProgressBar(textRenderer, graphics, left, y + getEmptyHeight(textRenderer) + 4);
        this.extractBoundItems(textRenderer, graphics, left, y + getEmptyHeight(textRenderer) + 17);
    }

    private void extractNonEmpty(Font textRenderer, int x, int y, int width, GuiGraphicsExtractor graphics) {
        int left = x + this.getXMargin(width);
        int itemOffset = left + 96;
        for (int i = 1; i <= 4; i++) {
            int xPos = itemOffset - i * 24;
            if (this.contents.getStacks().size() >= i) {
                this.extractItem(textRenderer, graphics, xPos, y, i);
            }
        }

        this.extractSelectedTooltip(textRenderer, graphics, x, y, width);
        this.extractProgressBar(textRenderer, graphics, left, y + this.getRowsHeight() + 4);
        this.extractBoundItems(textRenderer, graphics, left, y + this.getRowsHeight() + 17);
    }

    private void extractSelectedTooltip(Font renderer, GuiGraphicsExtractor graphics, int x, int y, int width) {
        if (!this.contents.hasSelected())
            return;

        ItemStack selected = this.contents.getSelectedStack().create();
        Component name = selected.getStyledHoverName();
        int textWidth = renderer.width(name.getVisualOrderText());
        int xPos = x + width / 2 - 12;
        ClientTooltipComponent component = ClientTooltipComponent.create(name.getVisualOrderText());
        graphics.tooltip(
          renderer,
          List.of(component),
          xPos - textWidth / 2,
          y - 15,
          DefaultTooltipPositioner.INSTANCE,
          selected.get(DataComponents.TOOLTIP_STYLE)
        );
    }

    private void extractBoundItems(Font textRenderer, GuiGraphicsExtractor graphics, int x, int y) {
        if (this.contents.getBoundEquipment().isEmpty())
            return;

        graphics.textWithWordWrap(textRenderer, BUNDLE_BOUND_ITEMS_DESCRIPTION, x, y + 2, 96, 16777215);

        int index = 0;
        for (ItemStackTemplate stack : this.contents.getBoundEquipment().values()) {
            graphics.item(stack.create(), x + 4 + index * 24, y + this.getBoundDescriptionHeight(textRenderer) + 6);
            index++;
        }
    }

    private void extractItem(Font renderer, GuiGraphicsExtractor graphics, int x, int y, int index) {
        int stackIndex = this.contents.getStacks().size() - index;
        ItemStackTemplate stack = this.contents.getStacks().get(stackIndex);
        if (stackIndex == this.contents.getSelected())
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE, x, y, 24, 24);
        else graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x, y, 24, 24);

        graphics.item(stack.create(), x + 4, y + 4, index);
        graphics.itemDecorations(renderer, stack.create(), x + 4, y + 4);
        if (stackIndex == this.contents.getSelected())
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE, x, y, 24, 24);
    }

    private void extractProgressBar(Font renderer, GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getProgressBarTexture(), x + 1, y, this.getProgressBarWidth(), 13);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE, x, y, 96, 13);
        if (this.contents.getStacks().isEmpty()) {
            graphics.centeredText(renderer, BUNDLE_EMPTY, x + 48, y + 3, 0xFFFFFFFF);
        }

        if (this.contents.getStacks().size() >= ArmourBundleContentsComponent.MAX_STACKS) {
            graphics.centeredText(renderer, BUNDLE_FULL, x + 48, y + 3, 0xFFFFFFFF);
        }
    }

    private Identifier getProgressBarTexture() {
        return this.contents.getStacks().size() >= ArmourBundleContentsComponent.MAX_STACKS ? BUNDLE_PROGRESS_BAR_FULL_TEXTURE : BUNDLE_PROGRESS_BAR_FILL_TEXTURE;
    }

    private int getProgressBarWidth() {
        return (int) Math.clamp(94 * (this.contents.getStacks().size() / (double) ArmourBundleContentsComponent.MAX_STACKS), 0, 94);
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
    public int getHeight(@NonNull Font textRenderer) {
        return this.contents.isEmpty() ? this.getEmptyHeight(textRenderer) + this.getBoundRowHeight(textRenderer) + 21 : this.getRowsHeight() + this.getBoundRowHeight(textRenderer) + 21;
    }

    @Override
    public int getWidth(@NonNull Font textRenderer) {
        return 96;
    }
}
