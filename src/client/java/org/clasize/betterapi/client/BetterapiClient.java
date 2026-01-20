package org.clasize.betterapi.client;

import net.fabricmc.api.ClientModInitializer;

public class BetterapiClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(Tracker::onTick);
        
        // Register Overlay (HUD)
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(new BetterapiOverlay());

        // Register Command
        net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback.EVENT.register(BetterapiCommand::register);
    }
}
