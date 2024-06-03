package com.auroali.armourbundles.items.tooltipdata;

import com.auroali.armourbundles.items.ArmourBundleInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ArmourBundleTooltipComponent implements TooltipComponent {
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("container/bundle/background");
    private static final int field_32381 = 4;
    private static final int field_32382 = 1;
    private static final int WIDTH_PER_COLUMN = 18;
    private static final int HEIGHT_PER_ROW = 20;
    private final ArmourBundleInventory inv;

    public ArmourBundleTooltipComponent(ArmourBundleInventory data) {
        this.inv = data;
    }

    @Override
    public int getHeight() {
        return this.getRowsHeight() + 4;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.getColumnsWidth();
    }

    private int getColumnsWidth() {
        return this.getColumns() * 18 + 2;
    }

    private int getRowsHeight() {
        return this.getRows() * 20 + 2;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int columns = this.getColumns();
        int rows = this.getRows();
        context.drawGuiTexture(BACKGROUND_TEXTURE, x, y, this.getColumnsWidth(), this.getRowsHeight());
        int k = 0;

        for(int row = 0; row < rows; ++row) {
            for(int column = 0; column < columns; ++column) {
                int slotX = x + column * 18 + 1;
                int slotY = y + row * 20 + 1;
                this.drawSlot(slotX, slotY, k++, context, textRenderer);
            }
        }
    }

    private void drawSlot(int x, int y, int index, DrawContext context, TextRenderer textRenderer) {
        if (index >= this.inv.stacks().size()) {
            this.draw(context, x, y, SlotSprite.SLOT);
        } else {
            ItemStack itemStack = this.inv.stacks().get(index);
            this.draw(context, x, y, SlotSprite.SLOT);
            context.drawItem(itemStack, x + 1, y + 1, index);
            context.drawItemInSlot(textRenderer, itemStack, x + 1, y + 1);
            if (index == 0) {
                HandledScreen.drawSlotHighlight(context, x + 1, y + 1, 0);
            }
        }
    }

    private void draw(DrawContext context, int x, int y, SlotSprite sprite) {
        context.drawGuiTexture(sprite.texture, x, y, 0, sprite.width, sprite.height);
    }

    private int getColumns() {
        return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.inv.stacks().size() + 1.0)));
    }

    private int getRows() {
        return (int)Math.min(3, Math.ceil(((double)this.inv.stacks().size() + 1.0) / (double)this.getColumns()));
    }

    @Environment(EnvType.CLIENT)
    static enum SlotSprite {
        BLOCKED_SLOT(new Identifier("container/bundle/blocked_slot"), 18, 20),
        SLOT(new Identifier("container/bundle/slot"), 18, 20);

        public final Identifier texture;
        public final int width;
        public final int height;

        private SlotSprite(final Identifier texture, final int width, final int height) {
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }
}
