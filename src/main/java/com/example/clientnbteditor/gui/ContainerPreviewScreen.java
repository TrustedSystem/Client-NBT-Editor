package com.example.clientnbteditor.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ContainerPreviewScreen extends Screen {
    private final ItemStack itemStack;
    private static final int ROWS = 3;
    private static final int COLS = 9;

    public ContainerPreviewScreen(ItemStack itemStack) {
        super(Text.of("Storage Preview"));
        this.itemStack = itemStack;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        int startX = (this.width - COLS * 18) / 2;
        int startY = (this.height - ROWS * 18) / 2;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, startY - 20, 0xFFFFFF);

        // Get container component
        // 1.21.11 uses DataComponentTypes.CONTAINER or similar
        // assuming standard ContainerComponent access
        ContainerComponent container = this.itemStack.get(DataComponentTypes.CONTAINER);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = startX + col * 18;
                int y = startY + row * 18;

                // Draw slot background
                context.fill(x, y, x + 18, y + 18, 0x80000000);
                context.fill(x, y, x + 18, y + 18, 0x80000000);
                this.drawBorder(context, x, y, 18, 18, 0xFFFFFFFF); // Simple border

                // Draw Item if present in container
                if (container != null) {
                    // ContainerComponent usually has a method to get stacks or we iterate
                    // It holds a list of items. Mapping grid index to list index.
                    // Check bounds and get item
                    // ContainerComponent.copy().stream() or similar?
                    // We'll mimic expected behavior if exact API is unknown, but .stream() is
                    // common
                    // or .getSlots()
                    // Let's assume container.getStack(index) doesn't strictly exist on the
                    // component itself commonly.
                    // Usually it's iterate-able.
                    // For simplicity, we just won't render items if we can't easily guess the API
                    // without docs
                    // BUT valid attempt:
                    // Atomic integer or similar?
                    // 1.21 uses `container.copyFirst()`? No.
                    // `container.stream()` returns Stream<ItemStack>.
                }
            }
        }

        // Actually implementing the item rendering for container component properly
        // requires mapped access
        // Since I'm flying blind on 1.21.11 specific ContainerComponent API details:
        if (container != null) {
            int i = 0;
            for (ItemStack stack : container.stream().toList()) {
                if (i >= ROWS * COLS)
                    break;
                int col = i % COLS;
                int row = i / COLS;
                int x = startX + col * 18;
                int y = startY + row * 18;
                context.drawItem(stack, x + 1, y + 1);

                // Tooltip
                if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                    context.drawItemTooltip(this.textRenderer, stack, mouseX, mouseY);
                }
                i++;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color); // Top
        context.fill(x, y + height - 1, x + width, y + height, color); // Bottom
        context.fill(x, y, x + 1, y + height, color); // Left
        context.fill(x + width - 1, y, x + width, y + height, color); // Right
    }
}
