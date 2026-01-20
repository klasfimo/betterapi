package org.clasize.betterapi.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class Tracker {
    private static final String TARGET_SERVER = "play.mavibugday.com";
    private static int tickCounter = 0;
    private static final int INTERVAL = 100; // 5 seconds

    public static void onTick(MinecraftClient client) {
        if (client.player == null || client.getCurrentServerEntry() == null) return;

        tickCounter++;
        if (tickCounter >= INTERVAL) {
            tickCounter = 0;
            String currentIp = client.getCurrentServerEntry().address;
            
            // Debug message in chat
            client.player.sendMessage(net.minecraft.text.Text.of("§a[BetterAPI] §7Heartbeat gönderiliyor... IP: " + currentIp), false);
            
            NetworkManager.sendHeartbeat(client.player.getName().getString(), currentIp);
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
