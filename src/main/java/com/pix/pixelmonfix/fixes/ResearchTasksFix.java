package com.pix.pixelmonfix.fixes;

import com.pixelmonmod.pixelmon.api.context.ContextKeys;
import com.pixelmonmod.pixelmon.api.context.StoredContext;
import com.pixelmonmod.pixelmon.api.events.PokeStopEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.npc.interaction.event.InteractionEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonInteractionEvents;
import com.pixelmonmod.pixelmon.api.storage.research.ResearchStorage;
import com.pixelmonmod.pixelmon.api.storage.research.ResearchStorageProxy;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.EntityParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.entities.npcs.NPC;
import com.pix.pixelmonfix.PixelmonFixMain;
import com.pix.pixelmonfix.config.ConfigManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResearchTasksFix {

    private static final List<ResourceKey<InteractionEvent>> pokestopEvents = new ArrayList<>();
    private static final List<ResourceKey<InteractionEvent>> bossEvents = new ArrayList<>();
    private static final List<ResourceKey<InteractionEvent>> trainerEvents = new ArrayList<>();
    private static boolean keysCached = false;

    @SuppressWarnings("unchecked")
    private static void cacheKeys() {
        if (keysCached) return;
        try {
            for (Field field : PixelmonInteractionEvents.class.getDeclaredFields()) {
                if (field.getType() == ResourceKey.class) {
                    ResourceKey<?> key = (ResourceKey<?>) field.get(null);
                    String path = key.location().getPath().toLowerCase();

                    if (path.contains("pokestop")) {
                        pokestopEvents.add((ResourceKey<InteractionEvent>) key);
                    }
                    if (path.contains("boss") || path.contains("mega")) {
                        bossEvents.add((ResourceKey<InteractionEvent>) key);
                    }
                    if (path.contains("trainer") || path.contains("gym") || path.contains("leader") || path.contains("tesla") || path.contains("circuit")) {
                        trainerEvents.add((ResourceKey<InteractionEvent>) key);
                    }
                }
            }
        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("Failed to map Interaction Event Keys", e);
        }
        keysCached = true;
    }

    @SubscribeEvent
    public void onPokeStopSpin(PokeStopEvent.Drops.Post event) {
        try {
            if (!ConfigManager.CONFIG.enableResearchFix) return;

            if (!(event.getPlayer() instanceof ServerPlayer)) return;
            ServerPlayer player = (ServerPlayer) event.getPlayer();

            cacheKeys();

            fireInteractionEvents(player, pokestopEvents, null);
        } catch (Exception e) {

        }
    }

    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        try {
            if (!ConfigManager.CONFIG.enableResearchFix) return;

            boolean wasBossDefeated = false;
            boolean wasTrainerDefeated = false;
            NPC defeatedNPC = null;

            for (BattleParticipant participant : event.getBattleController().participants) {

                if (participant instanceof WildPixelmonParticipant wild) {
                    for (PixelmonWrapper wrapper : wild.getTeamPokemon()) {
                        if (wrapper.getEntity() != null && wrapper.getEntity().isBossPokemon()) {
                            wasBossDefeated = true;
                            break;
                        }
                    }
                }
                else if (participant.isTrainer()) {
                    boolean hasAlive = false;
                    for (PixelmonWrapper pw : participant.allPokemon) {
                        if (pw.isAlive()) {
                            hasAlive = true;
                            break;
                        }
                    }
                    if (!hasAlive) {
                        wasTrainerDefeated = true;


                        if (participant instanceof EntityParticipant ep) {
                            if (ep.getEntity() instanceof NPC npc) {
                                defeatedNPC = npc;
                            }
                        }
                    }
                }
            }

            if (!wasBossDefeated && !wasTrainerDefeated) return;

            cacheKeys();
            for (BattleParticipant participant : event.getBattleController().participants) {
                if (participant instanceof PlayerParticipant playerParticipant && playerParticipant.player != null) {
                    if (wasBossDefeated) {
                        fireInteractionEvents(playerParticipant.player, bossEvents, null);
                    }
                    if (wasTrainerDefeated) {

                        fireInteractionEvents(playerParticipant.player, trainerEvents, defeatedNPC);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private void fireInteractionEvents(ServerPlayer player, List<ResourceKey<InteractionEvent>> eventKeys, NPC optionalNpc) {

        player.server.execute(() -> {
            try {
                ResourceKey<Registry<InteractionEvent>> registryKey = ResourceKey.createRegistryKey(ResourceLocation.parse("pixelmon:interaction_event"));
                Registry<InteractionEvent> registry = player.registryAccess().registry(registryKey).orElse(null);

                if (registry == null) return;

                ResearchStorage research = ResearchStorageProxy.getStorageNow(player);
                if (research != null) {
                    StoredContext context = StoredContext.of(ContextKeys.PLAYER, player);


                    if (optionalNpc != null) {
                        context.setContext(ContextKeys.NPC, optionalNpc);
                    }

                    for (ResourceKey<InteractionEvent> key : eventKeys) {
                        Optional<Holder.Reference<InteractionEvent>> holder = registry.getHolder(key);
                        holder.ifPresent(interactionEventReference -> research.handleInteractionEvent(interactionEventReference, context));
                    }
                }
            } catch (Exception e) {
            }
        });
    }
}