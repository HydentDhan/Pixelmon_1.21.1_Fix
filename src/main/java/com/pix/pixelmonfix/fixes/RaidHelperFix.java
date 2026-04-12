package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.raids.EndRaidEvent;
import com.pixelmonmod.pixelmon.battles.raids.RaidData;
import com.pix.pixelmonfix.PixelmonFixMain;
import com.pix.pixelmonfix.config.ConfigManager;
import com.pix.pixelmonfix.utils.DiscordWebhook;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;

import java.util.ArrayList;
import java.util.List;

public class RaidHelperFix {

    @SubscribeEvent
    public void onRaidEnd(EndRaidEvent event) {
        try {
            if (!ConfigManager.CONFIG.enableRaidHelperFix) return;

            RaidData raid = event.getRaid();
            List<String> realPlayers = new ArrayList<>();

            for (RaidData.RaidPlayer player : raid.getPlayers()) {
                if (player.isPlayer() && player.name != null) {
                    realPlayers.add(player.name);
                }

                if (!player.isPlayer() && player.pokemon != null) {
                    player.pokemon.getPixelmonEntity().ifPresent(entity -> {
                        entity.getPersistentData().putBoolean("pixelmonfix_raid_helper", true);
                        entity.discard();
                    });
                }
            }

            String outcome = event.didRaidersWin() ? "DEFEATED" : "FAILED";
            String bossName = raid.getPokemon() != null ? raid.getPokemon().getLocalizedName() : "Unknown Boss";

            PixelmonFixMain.LOGGER.info(String.format(
                    "[Raid Audit] %s a %d-Star %s Raid. Participants: %s",
                    outcome, raid.getStars(), bossName, String.join(", ", realPlayers)
            ));

        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] RaidHelperFix (EndRaid) Error", e);
            DiscordWebhook.sendError("Raid Helper End Despawn", e);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCapturePre(CaptureEvent.StartCapture event) {
        try {
            if (!ConfigManager.CONFIG.enableRaidHelperFix) return;

            if (event.getPokemon() != null) {
                event.getPokemon().getPixelmonEntity().ifPresent(entity -> {
                    CompoundTag data = entity.getPersistentData();
                    if (data.getBoolean("pixelmonfix_raid_helper")) {
                        event.setCanceled(true);
                        if (event.getPokeBallEntity() != null) {
                            event.getPokeBallEntity().discard();
                        }
                    }
                });
            }

        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] RaidHelperFix (StartCapture) Error", e);
            DiscordWebhook.sendError("Raid Helper Capture Block", e);
        }
    }
}