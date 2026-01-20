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

    public static String lastStatus = "Bekleniyor...";
    public static String lastError = null;
    public static long lastErrorTime = 0;
    public static long lastSuccessTime = 0;

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
                        lastStatus = "§aBağlı (200 OK)";
                        lastSuccessTime = System.currentTimeMillis();
                        lastError = null; // Clear error on success

                        JsonObject res = gson.fromJson(response.body(), JsonObject.class);
                        if (res.has("scanRequested") && res.get("scanRequested").getAsBoolean()) {
                            Tracker.performScan();
                        }
                    } else if (response.statusCode() == 403 || response.statusCode() == 401) {
                        lastStatus = "§cYetkilendirme Hatası (" + response.statusCode() + ")";
                        lastError = "API Key geçersiz, yeniden kayıt olunuyor... (" + response.statusCode() + ")";
                        lastErrorTime = System.currentTimeMillis();
                        
                        // Reset API Key to trigger re-registration
                        ConfigManager.get().apiKey = "CHANGE_ME";
                        ConfigManager.save();
                        System.err.println("BetterAPI Auth Failed. Resetting Key to re-register.");
                    } else {
                        lastStatus = "§eHata (" + response.statusCode() + ")";
                        lastError = "API Hatası: " + response.statusCode();
                        lastErrorTime = System.currentTimeMillis();
                        System.err.println("BetterAPI Auth Failed. Key might be invalid.");
                    }
                })
                .exceptionally(e -> {
                    lastStatus = "§4Bağlantı Hatası";
                    lastError = "Bağlantı Hatası: " + e.getMessage();
                    lastErrorTime = System.currentTimeMillis();
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
