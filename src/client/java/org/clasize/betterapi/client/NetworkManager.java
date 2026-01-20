package org.clasize.betterapi.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class NetworkManager {
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();

    public static void registerUser(String username) {
        String url = ConfigManager.get().apiUrl + "/register";
        JsonObject json = new JsonObject();
        json.addProperty("username", username);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject res = gson.fromJson(response.body(), JsonObject.class);
                        if (res.has("apiKey")) {
                            String newKey = res.get("apiKey").getAsString();
                            System.out.println("BetterAPI Auto-Registered! Key: " + newKey);
                            ConfigManager.get().apiKey = newKey;
                            ConfigManager.save();
                        }
                    } else {
                        System.err.println("BetterAPI Registration Failed: " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    System.err.println("BetterAPI Registration Network Error: " + e.getMessage());
                    return null;
                });
    }

    public static void sendHeartbeat(String username, String serverIp) {
        // Auto-Register if API Key is missing or default
        if (ConfigManager.get().apiKey.equals("CHANGE_ME") || ConfigManager.get().apiKey.isEmpty()) {
            registerUser(username);
            return;
        }

        String url = ConfigManager.get().apiUrl + "/heartbeat";
        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("serverIp", serverIp);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", ConfigManager.get().apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        // Success debug
                        net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                            if(net.minecraft.client.MinecraftClient.getInstance().player != null)
                                net.minecraft.client.MinecraftClient.getInstance().player.sendMessage(net.minecraft.text.Text.of("§a[BetterAPI] §7Sunucuya ulaşıldı! (200 OK)"), false);
                        });

                        JsonObject res = gson.fromJson(response.body(), JsonObject.class);
                        if (res.has("scanRequested") && res.get("scanRequested").getAsBoolean()) {
                            Tracker.performScan();
                        }
                    } else if (response.statusCode() == 403 || response.statusCode() == 401) {
                         // Error debug
                         net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                            if(net.minecraft.client.MinecraftClient.getInstance().player != null)
                                net.minecraft.client.MinecraftClient.getInstance().player.sendMessage(net.minecraft.text.Text.of("§6[BetterAPI] §7API Key geçersiz, yeniden kayıt olunuyor..."), false);
                        });
                        
                        // Reset API Key to trigger re-registration
                        ConfigManager.get().apiKey = "CHANGE_ME";
                        ConfigManager.save();
                        System.err.println("BetterAPI Auth Failed. Resetting Key to re-register.");
                    } else {
                         // Error debug
                         net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                            if(net.minecraft.client.MinecraftClient.getInstance().player != null)
                                net.minecraft.client.MinecraftClient.getInstance().player.sendMessage(net.minecraft.text.Text.of("§c[BetterAPI] §7Hata: " + response.statusCode()), false);
                        });
                        System.err.println("BetterAPI Auth Failed. Key might be invalid.");
                    }
                })
                .exceptionally(e -> {
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        if(net.minecraft.client.MinecraftClient.getInstance().player != null)
                            net.minecraft.client.MinecraftClient.getInstance().player.sendMessage(net.minecraft.text.Text.of("§4[BetterAPI] §7Bağlantı Hatası: " + e.getMessage()), false);
                    });
                    System.err.println("BetterAPI Heartbeat Failed: " + e.getMessage());
                    return null;
                });
    }

    public static void sendTabList(java.util.List<String> players) {
        if (ConfigManager.get().apiKey.equals("CHANGE_ME")) return;

        String url = ConfigManager.get().apiUrl + "/tablist";
        JsonObject json = new JsonObject();
        json.add("players", gson.toJsonTree(players));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", ConfigManager.get().apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(json)))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    System.out.println("BetterAPI TabList Sent: " + response.statusCode());
                });
    }
}
