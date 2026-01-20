package org.clasize.betterapi.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tracker {
    private static int tickCounter = 0;
    private static final int INTERVAL = 100; // 5 seconds

    public static void onTick(MinecraftClient client) {
        if (client.player == null || client.getCurrentServerEntry() == null) return;

        final String targetServer = ConfigManager.get().targetServer;
        final String currentIp = client.getCurrentServerEntry().address;
        if (targetServer != null && !targetServer.isEmpty()) {
            final String normalizedTarget = targetServer.toLowerCase();
            final String normalizedCurrent = currentIp.toLowerCase();
            if (!normalizedCurrent.contains(normalizedTarget)) {
                return; // different server, skip heartbeat entirely
            }
        }

        tickCounter++;
        if (tickCounter >= INTERVAL) {
            tickCounter = 0;
            if (NetworkManager.isHeartbeatReady()) {
                NetworkManager.sendHeartbeat(client.player.getName().getString(), currentIp);
            }
        }
    }

    public static void performScan() {
        MinecraftClient client = MinecraftClient.getInstance();
        // Ensure we are on main thread if possible, or handle gracefully
        client.execute(() -> {
            if (client.getNetworkHandler() == null) return;

            Collection<PlayerListEntry> entries = client.getNetworkHandler().getPlayerList();
            List<String> playerNames = new ArrayList<>();
            
            for (PlayerListEntry entry : entries) {
                if (entry.getProfile() != null && entry.getProfile().getName() != null) {
                    playerNames.add(entry.getProfile().getName());
                }
            }
            
            // Send async
            NetworkManager.sendTabList(playerNames);
        });
    }
}
