package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.storage.research.ResearchStorage;
import com.pixelmonmod.pixelmon.api.storage.research.ResearchStorageProxy;
import com.pix.pixelmonfix.PixelmonFixMain;
import com.pix.pixelmonfix.config.ConfigManager;
import com.pix.pixelmonfix.utils.DiscordWebhook;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class ResearchPersistenceFix {

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        try {
            if (!ConfigManager.CONFIG.enableResearchFix) return;

            if (event.getEntity() instanceof ServerPlayer player) {
                ResearchStorage research = ResearchStorageProxy.getStorageNow(player);
                if (research != null) {
                    CompoundTag data = research.serialize();
                    if (data != null && !data.isEmpty()) {

                        player.getPersistentData().put("pixelmonfix_research_backup", data);
                    }
                }
            }
        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] Research Backup Save Error", e);
            DiscordWebhook.sendError("Research Save Backup", e);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            if (!ConfigManager.CONFIG.enableResearchFix) return;

            if (event.getEntity() instanceof ServerPlayer player) {
                CompoundTag data = player.getPersistentData().getCompound("pixelmonfix_research_backup");

                if (data != null && !data.isEmpty()) {
                    ResearchStorageProxy.getStorage(player).thenAccept(research -> {
                        if (research != null) {
                            research.deserialize(data);
                        }
                    });
                }
            }
        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] Research Backup Load Error", e);
            DiscordWebhook.sendError("Research Load Backup", e);
        }
    }
}