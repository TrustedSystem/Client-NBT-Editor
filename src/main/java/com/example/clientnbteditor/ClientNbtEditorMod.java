package com.example.clientnbteditor;

import com.example.clientnbteditor.gui.ContainerPreviewScreen;
import com.example.clientnbteditor.gui.NbtEditorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ClientNbtEditorMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("editnbt")
                    .executes(context -> {
                        MinecraftClient client = context.getSource().getClient();
                        if (client.player != null) {
                            // Must run on render thread/next tick usually for screen switching from command
                            client.execute(
                                    () -> client.setScreen(new NbtEditorScreen(client.player.getMainHandStack())));
                        }
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("seestorage")
                    .executes(context -> {
                        MinecraftClient client = context.getSource().getClient();
                        if (client.player != null) {
                            client.execute(() -> client
                                    .setScreen(new ContainerPreviewScreen(client.player.getMainHandStack())));
                        }
                        return 1;
                    }));
        });
    }
}
