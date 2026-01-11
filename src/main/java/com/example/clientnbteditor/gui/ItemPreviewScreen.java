package com.example.clientnbteditor.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ItemPreviewScreen extends Screen {
    private final ItemStack itemStack;
    private final Screen parent;

    public ItemPreviewScreen(ItemStack itemStack, Screen parent) {
        super(Text.of("Preview"));
        this.itemStack = itemStack;
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        // Draw single slot at center
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Draw a slot texture or simple box
        context.fill(centerX - 10, centerY - 10, centerX + 10, centerY + 10, 0xFF888888);
        context.drawItem(this.itemStack, centerX - 8, centerY - 8);
        context.drawItemTooltip(this.textRenderer, this.itemStack, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Press ESC to return"), centerX, centerY + 20,
                0xFFFFFF);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
