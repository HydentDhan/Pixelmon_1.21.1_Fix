package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.battles.BattleStartTypes;
import com.pixelmonmod.pixelmon.api.events.PixelmonBlockTriggeredBattleEvent;
import com.pixelmonmod.pixelmon.api.events.curry.CurryFinishedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.blocks.spawning.BlockSpawningHandler;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pix.pixelmonfix.PixelmonFixMain;
import com.pix.pixelmonfix.config.ConfigManager;
import com.pix.pixelmonfix.utils.DiscordWebhook;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CurryCrashFix {

    private static final Map<UUID, Object[]> pendingCurrySpawns = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onCurryFinished(CurryFinishedEvent event) {
        try {
            if (!ConfigManager.CONFIG.enableCurryCrashFix) return;

            ServerPlayer player = event.getPlayer();
            if (player != null) {
                pendingCurrySpawns.put(player.getUUID(), new Object[]{event.getCookingFlavor(), event.getRating()});
            }
        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] CurryFinishedEvent Error", e);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBattleTrigger(PixelmonBlockTriggeredBattleEvent event) {
        try {
            if (!ConfigManager.CONFIG.enableCurryCrashFix) return;

            if (event.startType == BattleStartTypes.CURRY && event.pixelmon == null) {

                ServerPlayer player = event.player;
                if (player == null) return;

                event.setCanceled(true);

                Object[] extra = pendingCurrySpawns.remove(player.getUUID());
                if (extra == null) return;

                BlockPos pos = event.pos;
                Level level = event.world;

                ServerLifecycleHooks.getCurrentServer().execute(() -> {
                    try {
                        PlayerPartyStorage party = StorageProxy.getPartyNow(player);
                        if (party == null) return;

                        Pokemon firstAble = party.findOne(Pokemon::canBattle);
                        if (firstAble != null) {
                            PixelmonEntity activePixelmon = firstAble.getOrSpawnPixelmon(player);

                            BlockSpawningHandler.getInstance().performBattleStartCheck(
                                    level, pos, player, activePixelmon, BattleStartTypes.CURRY, null, extra
                            );
                        }
                    } catch (Exception innerException) {
                        PixelmonFixMain.LOGGER.error("[PixelmonFix] Failed to spawn corrected Curry battle", innerException);
                        DiscordWebhook.sendError("Curry Battle Re-Execution", innerException);
                    }
                });
            }
        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("[PixelmonFix] CurryCrashFix Error", e);
            DiscordWebhook.sendError("Curry Block Trigger Crash Prevent", e);
        }
    }
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            pendingCurrySpawns.remove(event.getEntity().getUUID());
        }
    }
}