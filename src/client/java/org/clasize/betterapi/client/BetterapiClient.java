package org.clasize.betterapi.client;

import net.fabricmc.api.ClientModInitializer;

public class BetterapiClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(Tracker::onTick);
    }
}
