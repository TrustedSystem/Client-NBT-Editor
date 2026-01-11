package com.example.clientnbteditor.mixin;

import com.example.clientnbteditor.gui.NbtEditorScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addOpenEditorButton(CallbackInfo ci) {
        // "On the down right corner, there will be a button of width 100 and height 20"
        // We'll place it at bottom right of the screen.
        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = this.width - buttonWidth - 5; // 5px padding from edge
        int y = this.height - buttonHeight - 5; // 5px padding from edge

        this.addDrawableChild(ButtonWidget.builder(Text.of("Open Editor"), (button) -> {
            if (this.client != null && this.client.player != null) {
                this.client.setScreen(new NbtEditorScreen(this.client.player.getMainHandStack()));
            }
        }).dimensions(x, y, buttonWidth, buttonHeight).build());
    }
}
