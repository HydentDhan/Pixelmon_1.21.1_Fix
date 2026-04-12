package com.pix.pixelmonfix.utils;

import com.pix.pixelmonfix.config.ConfigManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordWebhook {


    private static final Map<String, Long> lastErrorTimestamps = new ConcurrentHashMap<>();


    private static final long COOLDOWN_MS = 5000;

    public static void sendError(String context, Exception e) {
        String webhookUrl = ConfigManager.CONFIG.discordWebhookUrl;

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        Long lastTime = lastErrorTimestamps.get(context);


        if (lastTime != null && (currentTime - lastTime) < COOLDOWN_MS) {
            return;
        }


        lastErrorTimestamps.put(context, currentTime);


        String errorMessage = e.toString().replace("\"", "'").replace("\n", " ");
        String jsonPayload = "{\"content\": \"**[PixelmonFix] Critical Error Prevented!**\\n**Context:** " + context + "\\n**Error:** `" + errorMessage + "`\"}";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();


            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {

        }
    }
}