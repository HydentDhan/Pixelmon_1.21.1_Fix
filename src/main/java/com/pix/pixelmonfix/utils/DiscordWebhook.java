package com.pix.pixelmonfix.utils;

import com.pix.pixelmonfix.config.ConfigManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordWebhook {

    // Tracks the exact timestamp of the last fired error for each context
    private static final Map<String, Long> lastErrorTimestamps = new ConcurrentHashMap<>();

    // The cooldown period in milliseconds (5 seconds = 5000ms)
    private static final long COOLDOWN_MS = 5000;

    public static void sendError(String context, Exception e) {
        String webhookUrl = ConfigManager.CONFIG.discordWebhookUrl;

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return; // Silently do nothing if no webhook is configured
        }

        long currentTime = System.currentTimeMillis();
        Long lastTime = lastErrorTimestamps.get(context);

        // Check if this specific error context fired within the cooldown window
        if (lastTime != null && (currentTime - lastTime) < COOLDOWN_MS) {
            return; // Drop the request to prevent Discord API rate-limiting
        }

        // Update the registry with the current time
        lastErrorTimestamps.put(context, currentTime);

        // Clean up the error message to fit cleanly inside a Discord JSON payload
        String errorMessage = e.toString().replace("\"", "'").replace("\n", " ");
        String jsonPayload = "{\"content\": \"**[PixelmonFix] Critical Error Prevented!**\\n**Context:** " + context + "\\n**Error:** `" + errorMessage + "`\"}";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Send asynchronously so it never lags the main server thread
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            // If the webhook fails (e.g., discord is offline or URL is invalid), silently ignore
        }
    }
}