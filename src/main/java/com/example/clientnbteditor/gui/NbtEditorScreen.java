package com.example.clientnbteditor.gui;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
public class NbtEditorScreen extends Screen {
    private final ItemStack originalStack;
    private EditBoxWidget editor;
    private ButtonWidget saveButton;
    private ButtonWidget previewButton;
    private long saveSuccessTime = 0;
    private boolean isInvalidMode = false;

    public NbtEditorScreen(ItemStack itemStack) {
        super(Text.of("NBT Editor"));
        this.originalStack = itemStack;
    }

    @Override
    protected void init() {
        super.init();

        int editorX = 20;
        int editorY = 40;
        int editorWidth = this.width - 40;
        int editorHeight = this.height - 60;

        String initialString = "{}";

        if (this.client != null && this.client.world != null) {
            try {
                // In 1.21.11, use RegistryOps to ensure complex components (like enchantments)
                // are serialized
                RegistryWrapper.WrapperLookup lookup = this.client.world.getRegistryManager();
                RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, lookup);
                NbtElement element = ItemStack.CODEC.encodeStart(ops, this.originalStack).getOrThrow();
                initialString = NbtHelper.toPrettyPrintedText(element).getString();
            } catch (Exception e) {
                initialString = "{}";
            }
        }

        // Correct 1.21 Builder pattern for EditBoxWidget
        this.editor = new EditBoxWidget.Builder()
                .x(editorX)
                .y(editorY)
                .build(this.textRenderer, editorWidth, editorHeight, Text.of("NBT Code"));

        this.editor.setMaxLength(Integer.MAX_VALUE);
        this.editor.setText(initialString);

        this.addDrawableChild(this.editor);

        this.saveButton = ButtonWidget.builder(Text.of("Save"), button -> save())
                .dimensions(20, 10, 100, 20)
                .build();
        this.addDrawableChild(this.saveButton);

        this.previewButton = ButtonWidget.builder(Text.of("Preview"), button -> openPreview())
                .dimensions(130, 10, 100, 20)
                .build();
        this.addDrawableChild(this.previewButton);
    }

    private void save() {
        if (this.client == null || this.client.player == null || this.client.world == null)
            return;

        if (!this.client.player.isCreative()) {
            this.isInvalidMode = true;
            this.saveSuccessTime = System.currentTimeMillis();
            return;
        }

        try {
            String text = this.editor.getText();
            NbtCompound newTag = NbtCompoundArgumentType.nbtCompound().parse(new StringReader(text));

            // Use RegistryOps to ensure components are parsed correctly back to the stack
            RegistryWrapper.WrapperLookup lookup = this.client.world.getRegistryManager();
            RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, lookup);
            ItemStack newItem = ItemStack.CODEC.parse(ops, newTag).result().orElse(ItemStack.EMPTY);

            if (!newItem.isEmpty()) {
                int slotIndex = 0;
                try {
                    Field f = this.client.player.getInventory().getClass().getDeclaredField("selectedSlot");
                    f.setAccessible(true);
                    slotIndex = f.getInt(this.client.player.getInventory());
                } catch (Exception e) {
                    // Fallback
                }

                this.client.player.getInventory().setStack(slotIndex, newItem.copy());
                int packetSlot = slotIndex + 36;
                this.client.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(packetSlot, newItem));

                this.saveSuccessTime = System.currentTimeMillis();
                this.isInvalidMode = false;
            }
        } catch (CommandSyntaxException e) {
        }
    }

    private void openPreview() {
        if (this.client == null || this.client.world == null)
            return;
        try {
            String text = this.editor.getText();
            NbtCompound newTag = NbtCompoundArgumentType.nbtCompound().parse(new StringReader(text));
            RegistryWrapper.WrapperLookup lookup = this.client.world.getRegistryManager();
            RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, lookup);
            ItemStack newItem = ItemStack.CODEC.parse(ops, newTag).result().orElse(this.originalStack);
            this.client.setScreen(new ItemPreviewScreen(newItem, this));
        } catch (CommandSyntaxException e) {
            this.client.setScreen(new ItemPreviewScreen(this.originalStack, this));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        long timeSince = System.currentTimeMillis() - this.saveSuccessTime;
        if (timeSince < 1000 && this.saveSuccessTime != 0) {
            if (this.isInvalidMode) {
                this.saveButton
                        .setMessage(Text.literal("Invalid Gamemode").formatted(net.minecraft.util.Formatting.RED));
            } else {
                this.saveButton.setMessage(Text.literal("Saved").formatted(net.minecraft.util.Formatting.GREEN));
            }
        } else {
            this.saveButton.setMessage(Text.of("Save"));
            if (timeSince >= 1000 && this.saveSuccessTime != 0 && !isInvalidMode) {
                this.close();
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
