package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pix.pixelmonfix.PixelmonFixMain;
import com.pix.pixelmonfix.config.ConfigManager;
import com.pix.pixelmonfix.utils.DiscordWebhook;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class ExpBarFix {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onExpGain(ExperienceGainEvent event) {
        try {
            if (!ConfigManager.CONFIG.enableExpBarFix) return;
            if (!event.isFromBattle() || event.isCanceled() || event.getExperience() <= 0) return;

            BattleController bc = event.getBattleController();
            if (bc == null) return;

            for (BattleParticipant participant : bc.participants) {
                if (participant instanceof PlayerParticipant) {
                    PlayerParticipant player = (PlayerParticipant) participant;

                    for (PixelmonWrapper wrapper : player.getTeamPokemon()) {
                        if (wrapper.getPokemonUUID().equals(event.pokemon.getUUID())) {

                            ServerLifecycleHooks.getCurrentServer().execute(() -> {
                                try {
                                    if (player.player != null && !player.player.hasDisconnected()) {
                                        player.updateBattlingPokemon();
                                    }
                                } catch (Exception innerException) {
                                    PixelmonFixMain.LOGGER.error("[PixelmonFix] Failed Exp UI sync", innerException);
                                    DiscordWebhook.sendError("Exp Bar UI Packet Sync", innerException);
                                }
                            });

                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] ExpBarFix Error", e);
            DiscordWebhook.sendError("Exp Bar Calculation Hook", e);
        }
    }
}