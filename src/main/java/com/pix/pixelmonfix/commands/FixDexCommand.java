package com.pix.pixelmonfix.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.pixelmonmod.pixelmon.api.pokedex.data.DexData;
import com.pixelmonmod.pixelmon.api.pokedex.data.PlayerDexData;
import com.pixelmonmod.pixelmon.api.pokedex.status.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokedex.status.PokedexState;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBase;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pix.pixelmonfix.PixelmonFixMain;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class FixDexCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pixelmonfix")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("fixdex")
                        .then(Commands.literal("all")
                                .executes(FixDexCommand::executeFixDexAll)
                        )
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(FixDexCommand::executeFixDex)
                        )
                )
        );
    }

    private static int executeFixDexAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
        int successCount = 0;

        for (ServerPlayer player : players) {
            if (fixPlayerDex(player, source)) {
                successCount++;
            }
        }

        final int finalSuccessCount = successCount;
        source.sendSuccess(() -> Component.literal("§aSuccessfully repaired Pokédex for §e" + finalSuccessCount + "§a online players."), true);
        return successCount;
    }

    private static int executeFixDex(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            fixPlayerDex(targetPlayer, context.getSource());
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§cAn error occurred: " + e.getMessage()));
            PixelmonFixMain.LOGGER.error("FixDex Error", e);
        }
        return 1;
    }

    @SuppressWarnings("unchecked")
    private static boolean fixPlayerDex(ServerPlayer targetPlayer, CommandSourceStack source) {
        try {
            PlayerPartyStorage party = StorageProxy.getPartyNow(targetPlayer);

            if (party == null) {
                source.sendFailure(Component.literal("Could not load party data for player: " + targetPlayer.getScoreboardName()));
                return false;
            }

            PlayerDexData targetDexData = null;

            try {
                Method getPokedex = party.getClass().getMethod("getPokedex");
                targetDexData = (PlayerDexData) getPokedex.invoke(party);
            } catch (Exception ignored) {}

            if (targetDexData == null) {
                Class<?> clazz = party.getClass();
                while (clazz != null && clazz != Object.class) {
                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        Object value = field.get(party);
                        if (value instanceof PlayerDexData) {
                            targetDexData = (PlayerDexData) value;
                            break;
                        }
                    }
                    if (targetDexData != null) break;
                    clazz = clazz.getSuperclass();
                }
            }

            if (targetDexData == null) {
                source.sendFailure(Component.literal("Could not locate Pokédex data for " + targetPlayer.getScoreboardName()));
                return false;
            }

            Field statusField = DexData.class.getDeclaredField("status");
            statusField.setAccessible(true);
            Map<PokemonBase, PokedexState> statusMap = (Map<PokemonBase, PokedexState>) statusField.get(targetDexData);

            int actualSeen = 0;
            int actualCaught = 0;

            for (PokedexState state : statusMap.values()) {
                if (state.status() == PokedexRegistrationStatus.CAUGHT) {
                    actualCaught++;
                    actualSeen++;
                } else if (state.status() == PokedexRegistrationStatus.SEEN) {
                    actualSeen++;
                }
            }

            Field seenField = DexData.class.getDeclaredField("seenCount");
            seenField.setAccessible(true);
            seenField.set(targetDexData, actualSeen);

            Field caughtField = DexData.class.getDeclaredField("caughtCount");
            caughtField.setAccessible(true);
            caughtField.set(targetDexData, actualCaught);

            targetDexData.initialize(targetPlayer);
            party.setNeedsSaving();

            final int finalCaught = actualCaught;
            final int finalSeen = actualSeen;

            source.sendSuccess(() -> Component.literal(
                    "§aSuccessfully repaired Pokédex for §e" + targetPlayer.getScoreboardName() + "§a. " +
                            "New Caught: §e" + finalCaught + "§a, New Seen: §e" + finalSeen
            ), false);

            return true;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cAn error occurred processing " + targetPlayer.getScoreboardName() + ": " + e.getMessage()));
            PixelmonFixMain.LOGGER.error("FixDex Error for " + targetPlayer.getScoreboardName(), e);
            return false;
        }
    }
}