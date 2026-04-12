package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.entities.pokeballs.PokeBallEntity;
import com.pix.pixelmonfix.PixelmonFixMain;
import com.pix.pixelmonfix.config.ConfigManager;
import com.pix.pixelmonfix.utils.DiscordWebhook;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;

public class CaptureFix {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCaptureSuccess(CaptureEvent.SuccessfulCapture event) {
        try {
            if (!ConfigManager.CONFIG.enableCaptureFix) return;

            PokeBallEntity ball = event.getPokeBallEntity();
            if (ball == null) return;

            CompoundTag data = ball.getPersistentData();

            if (data.getBoolean("pixelmonfix_processed")) {
                event.setCanceled(true);
                ball.discard();
                return;
            }

            data.putBoolean("pixelmonfix_processed", true);

        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] CaptureFix Error", e);
            DiscordWebhook.sendError("Infinite EXP Pokeball Event", e);
        }
    }
}